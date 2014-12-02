package com.anz.rer.etl.directory.impl;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.anz.rer.etl.csvToTable.Bcp;
import com.anz.rer.etl.csvToTable.BcpCleaner;
import com.anz.rer.etl.csvToTable.BcpConfig;
import com.anz.rer.etl.csvToTable.BcpData;
import com.anz.rer.etl.csvToTable.BcpDone;
import com.anz.rer.etl.csvToTable.BcpInsertPool;
import com.anz.rer.etl.csvToTable.BcpTransactionCoordinator;
import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.utils.FileUtils;
import com.anz.rer.etl.utils.GlobalStatusUtil;

public class BcpDirectoryFileProcessor extends ADirectoryWatcher {
    
	private String srcDirectory;
	private String filePattern;
	private DataSource dataSource;
	private JdbcTemplate jdbc_; 
	private int insertThread;
	private int fileProcessorThread;
	private int doneThread;
	
	private String ignoreString;
	private String columnDelimeter;
	private int dataPartition;
	
	private int sqlQueueSize;
	private int doneQueueSize;
	private int cleanUpQueueSize;
	private DbUtils dbUtils; 
	private boolean insertToHeader;
	private boolean insertToFact;
	private int retryThreshold;
	private int batchInsert;
	
	public int getBatchInsert() {
		return batchInsert;
	}

	public void setBatchInsert(int batchInsert) {
		this.batchInsert = batchInsert;
	}

	private BcpTransactionCoordinator bcpTranCoordinator;
	
	
	
	private Set<String> alreadyProcessedFiles = new HashSet<String>();
	
	private BlockingQueue<BcpData> sqlQueue; //    = new LinkedBlockingQueue<BcpData>(10);
	private BlockingQueue<BcpData> doneQueue;  //  = new LinkedBlockingQueue<BcpData>(5);
	private BlockingQueue<BcpData> cleanUpQueue;// = new LinkedBlockingQueue<BcpData>(5);
	
	private BlockingQueue<BcpData> fileQueue;// = new LinkedBlockingQueue<BcpData>(5);
	
	private GlobalStatusUtil globalStatusUtil;
	
	
	protected ExecutorService fileProcessor;
	
	private Map<String,BcpConfig> bcpConfig = new HashMap<String,BcpConfig>();
	
	
	private final static Logger logger = Logger.getLogger(BcpDirectoryFileProcessor.class);
	

	public BcpDirectoryFileProcessor(Properties props, int fileProcessorThread, int sqlQueueSize, int doneQueueSize,
            int cleanUpQueueSize, DbUtils dbUtils, boolean insertToHeader, boolean insertToFact,int retryThreshold) {
		super(props.getProperty("etl.csvToTable.src.directory"),
			  props.getProperty("etl.csvToTable.src.fileName.pattern"));
		
		this.srcDirectory = props.getProperty("etl.csvToTable.src.directory");
		this.setFilePattern(props.getProperty("etl.csvToTable.src.fileName.pattern"));
	
		fileProcessor =  Executors.newFixedThreadPool( fileProcessorThread ) ;
		fileQueue     = new ArrayBlockingQueue<BcpData>( fileProcessorThread ,true );
		sqlQueue      = new ArrayBlockingQueue<BcpData>(sqlQueueSize,true );
	
		 doneQueue    = new ArrayBlockingQueue<BcpData>(doneQueueSize,true);
		 cleanUpQueue = new ArrayBlockingQueue<BcpData>(cleanUpQueueSize,true);
		 this.dbUtils = dbUtils;
		 this.insertToHeader = insertToHeader;
		 this.insertToFact = insertToFact;
		 this.retryThreshold = retryThreshold;
		
	}

	public BcpDirectoryFileProcessor(String path, String filter) {
		super(path, filter);
		this.srcDirectory = path;
		this.setFilePattern(filter);
	}

	public BcpDirectoryFileProcessor(String path) {
		super(path);
		this.srcDirectory = path;
	}
	
	  
	public void initialize(){
		
		String scrDir = this.srcDirectory;
		String destDir = this.srcDirectory + File.separator + "archive" + File.separator;
		
		logger.info( "Source Directory:" + scrDir);
		logger.info( "Destination Directory:" + destDir);
		
		logger.info( "Insert To Header Table:" + this.insertToHeader);
		logger.info( "Insert To Fact Table:" + this.insertToFact);
		logger.info( "Retry Threshold:" + this.retryThreshold);
						
	
		
		bcpTranCoordinator = new BcpTransactionCoordinator();
		globalStatusUtil   = new GlobalStatusUtil();
		//jdbc = new JdbcTemplate(dataSource);
		
		logger.info("##############################################################################################################");
		try {
			logger.info(  dbUtils.getDataSource().getConnection().toString() );
		} catch (SQLException e1) {
			e1.printStackTrace();
		} 
		logger.info("##############################################################################################################");
		
			new Thread(new BcpInsertPool( sqlQueue, doneQueue, bcpTranCoordinator, dataSource,insertThread, batchInsert,bcpConfig ,cleanUpQueue , dbUtils),"DB Insert Thread" ).start();
			new Thread(new BcpDone(doneQueue, cleanUpQueue,doneThread, dbUtils,insertToHeader,insertToFact, retryThreshold,globalStatusUtil ), "Done Queue Thread").start();
			new Thread(new BcpCleaner( destDir,scrDir,  cleanUpQueue ),"CleanUp-Archiving").start();
		
		this.run();
		
	}
	
	
	@Override
	public void onChange(File file, String action) {
		try{
	 	if(!action.equals("delete")){
	
			if(alreadyProcessedFiles.contains(file.getName())){
				logger.info( String.format("File %s already processed", file.getName()));
				FileUtils.archiveFile(file.getAbsolutePath(), file.getParent()  + File.separator + "archive" + File.separator);
			 } else {
				 alreadyProcessedFiles.add(file.getName());
				 //Future<Long> f =
				 logger.debug("Submit:" + file.getAbsolutePath());
				 fileProcessor.submit(new Bcp( sqlQueue,file,this.getBcpConfig(file.getName()),dataPartition,columnDelimeter,ignoreString,cleanUpQueue, System.currentTimeMillis(),dbUtils  ));
				 	//f.get();
			 }  
		} else {
			logger.info("File was deleted or move to archive:" + file.getName());
		}
		}catch(Exception e){
			 e.printStackTrace();
		}
	}
	
	private BcpConfig getBcpConfig(String fileName){
		for ( Map.Entry<String, BcpConfig> bcpConf: bcpConfig.entrySet()) {
			 if(fileName.toUpperCase().contains( bcpConf.getKey().toUpperCase() )){
				return bcpConf.getValue();
			 }
		}
		return null;
	}
	
 	

 	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Map<String, BcpConfig> getBcpConfig() {
		return bcpConfig;
	}

	public void setBcpConfig(Map<String, BcpConfig> bcpConfig) {
		this.bcpConfig = bcpConfig;
	}
	
	public static void main(String[] args){
		//BcpDirectoryFileProcessor b =new BcpDirectoryFileProcessor("C:\\project\\data\\ApExtract\\", ".*non-var*..*apx"); //".*2010.*\\.txt"
		
		BcpDirectoryFileProcessor b =new BcpDirectoryFileProcessor("C:\\project\\data\\ApExtract\\", ".*.apx"); //".*2010.*\\.txt"
		
		b.run();
	
		TimerTask task =  (TimerTask) b; //.*ter\..*
		Timer timer = new Timer();
		timer.schedule(task, new Date(), 10000); 
		
		
			
		/*
		 String tableName = "";
		String fileName = "";
		
		
		
		 *
		 */
	}

	public void setInsertThread(int insertThread) {
		this.insertThread = insertThread;
	}

	public int getInsertThread() {
		return insertThread;
	}

	public void setFileProcessorThread(int fileProcessorThread) {
		this.fileProcessorThread = fileProcessorThread;
	}

	public int getFileProcessorThread() {
		return fileProcessorThread;
	}

	public void setDoneThread(int doneThread) {
		this.doneThread = doneThread;
	}

	public int getDoneThread() {
		return doneThread;
	}

	public void setIgnoreString(String ignoreString) {
		this.ignoreString = ignoreString;
	}

	public String getIgnoreString() {
		return ignoreString;
	}

	public void setColumnDelimeter(String columnDelimeter) {
		this.columnDelimeter = columnDelimeter;
	}

	public String getColumnDelimeter() {
		return columnDelimeter;
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setDataPartition(int dataPartition) {
		this.dataPartition = dataPartition;
	}

	public int getDataPartition() {
		return dataPartition;
	}

	public void setSqlQueueSize(int sqlQueueSize) {
		this.sqlQueueSize = sqlQueueSize;
	}

	public int getSqlQueueSize() {
		return sqlQueueSize;
	}

	public void setDoneQueueSize(int doneQueueSize) {
		this.doneQueueSize = doneQueueSize;
	}

	public int getDoneQueueSize() {
		return doneQueueSize;
	}

	public void setCleanUpQueueSize(int cleanUpQueueSize) {
		this.cleanUpQueueSize = cleanUpQueueSize;
	}

	public int getCleanUpQueueSize() {
		return cleanUpQueueSize;
	}

	public void setDbUtils(DbUtils dbUtils) {
		this.dbUtils = dbUtils;
	}

	public DbUtils getDbUtils() {
		return dbUtils;
	}

	public void setInsertToHeader(boolean insertToHeader) {
		this.insertToHeader = insertToHeader;
	}

	public boolean isInsertToHeader() {
		return insertToHeader;
	}

	public void setInsertToFact(boolean insertToFact) {
		this.insertToFact = insertToFact;
	}

	public boolean isInsertToFact() {
		return insertToFact;
	}

	public void setRetryThreshold(int retryThreshold) {
		this.retryThreshold = retryThreshold;
	}

	public int getRetryThreshold() {
		return retryThreshold;
	}

}
