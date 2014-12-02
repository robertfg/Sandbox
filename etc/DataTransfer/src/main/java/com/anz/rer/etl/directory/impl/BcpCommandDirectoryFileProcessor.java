package com.anz.rer.etl.directory.impl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.anz.rer.etl.csvToTable.BcpCleaner;
import com.anz.rer.etl.csvToTable.BcpConfig;
import com.anz.rer.etl.csvToTable.BcpData;
import com.anz.rer.etl.csvToTable.BcpDone;
import com.anz.rer.etl.csvToTable.BcpExecCommandPool;
import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.utils.FileUtils;
import com.anz.rer.etl.utils.GlobalStatusUtil;

public class BcpCommandDirectoryFileProcessor extends ADirectoryWatcher {
    
	private String srcDirectory;
	private String filePattern;
	private DataSource dataSource;
	private JdbcTemplate jdbc; 
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

	//private BcpTransactionCoordinator_ bcpTranCoordinator;
	private GlobalStatusUtil globalStatus;
	
	
	private Set<String> alreadyProcessedFiles = new HashSet<String>();
	
	private BlockingQueue<BcpData> cmdQueue; //    = new LinkedBlockingQueue<BcpData>(10);
	private BlockingQueue<BcpData> doneQueue;  //  = new LinkedBlockingQueue<BcpData>(5);
	private BlockingQueue<BcpData> cleanUpQueue;// = new LinkedBlockingQueue<BcpData>(5);
	
	private static final Map<String,String> varContainerMapping = new HashMap<String,String>();	
	static {
			varContainerMapping.put("B1AL0", "HYPO");
			varContainerMapping.put("VXAL0", "VAR_10D_AGG");
			varContainerMapping.put("V1AL0", "VAR_1D_AGG");
			varContainerMapping.put("VSAL0", "VAR_STRESS_AGG");
			varContainerMapping.put("VFAL0", "VAR_1540_AGG");
			
	}
	
	protected ExecutorService fileProcessor;
	
	private Map<String,BcpConfig> bcpConfig = new HashMap<String,BcpConfig>();
	
	
	private final static Logger logger = Logger.getLogger(BcpCommandDirectoryFileProcessor.class);
	

	public BcpCommandDirectoryFileProcessor(Properties props, int fileProcessorThread, int sqlQueueSize, int doneQueueSize,
            int cleanUpQueueSize, DbUtils dbUtils, boolean insertToHeader, boolean insertToFact,int retryThreshold) {
		super(props.getProperty("etl.csvToTable.src.directory"),
			  props.getProperty("etl.csvToTable.src.fileName.pattern"));
		
		this.srcDirectory = props.getProperty("etl.csvToTable.src.directory");
		this.setFilePattern(props.getProperty("etl.csvToTable.src.fileName.pattern"));
	
		 fileProcessor =  Executors.newFixedThreadPool( fileProcessorThread ) ;
		 cmdQueue     = new ArrayBlockingQueue<BcpData>(sqlQueueSize,true );
		 doneQueue    = new ArrayBlockingQueue<BcpData>(doneQueueSize,true);
		 cleanUpQueue = new ArrayBlockingQueue<BcpData>(cleanUpQueueSize,true);
		 this.dbUtils = dbUtils;
		 this.insertToHeader = insertToHeader;
		 this.insertToFact = insertToFact;
		 this.retryThreshold = retryThreshold;

	}

	public BcpCommandDirectoryFileProcessor(String path, String filter) {
		super(path, filter);
		this.srcDirectory = path;
		this.setFilePattern(filter);
	}

	public BcpCommandDirectoryFileProcessor(String path) {
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
						
		
	
		
		
		jdbc = new JdbcTemplate(dataSource);
		
		logger.info("##############################################################################################################");
		try {
			logger.info( jdbc.getDataSource().getConnection().toString() );
		} catch (SQLException e1) {
			e1.printStackTrace();
		} 
		logger.info("##############################################################################################################");
		
		    // CommandExecPool
			globalStatus = new GlobalStatusUtil();
			new Thread(new BcpExecCommandPool( cmdQueue, doneQueue, /*bcpTranCoordinator, */insertThread, bcpConfig,dbUtils,globalStatus,cleanUpQueue  ),"Exec Insert Thread" ).start();
			new Thread(new BcpDone(doneQueue, cleanUpQueue,doneThread, dbUtils,insertToHeader,insertToFact, retryThreshold, globalStatus ), "Done Queue Thread").start();
			new Thread(new BcpCleaner( destDir,scrDir,  cleanUpQueue ),"CleanUp-Archiving").start();
		
			this.run();
		
	}
	
	
	@Override
	public void onChange(File file, String action) {
		try{
	 
		if(!action.equals("delete")){
			
			if(alreadyProcessedFiles.contains(file.getName())){
				logger.info( String.format("File %s was already processed", file.getName()));
				
			
			} else {
			
				BcpData bcpData = this.buildBcpData(file);
		
				 if(canBcp(file.getName(),8)) {
					 logger.debug("The File will be BCP:" + bcpData.getVarType() + ",ExtractType:" + bcpData.getExtractType());
				    if( file.length()>0 ) {
				    	if( bcpData.getVarType().equals("VAR_1D") ||  bcpData.getVarType().equals("VAR_10D") 
				    			||  bcpData.getVarType().equals("VAR_STRESS") || bcpData.getVarType().equals("VAR_1540") ) {
				    	
				    		if(this.gotDbError(varContainerMapping.get(bcpData.getPsrCode()),  bcpData.getCobDate())) {
				    			cleanUpQueue.put( new BcpData( file.getName(),file.getPath(),"failed" ) ) ;
				    			globalStatus.getStatus().put( bcpData.getKey(), "error");
				    	    } else {
				    	    	/* check if confidence level was already processed */
								 if(bcpData.getName().contains("PNL-VAR_1D") || bcpData.getName().contains("PNL-VAR_10D") 
								 			||bcpData.getName().contains("PNL-VAR_STRESS") ||bcpData.getName().contains("PNL-VAR_1540") ) {
							
									 String confiKey    =  bcpData.getVarType() +"#" + "VAR_CONFIDENCE"    + "#" + bcpData.getCobDate();
									   if(bcpData.getVarType().equals("VAR_STRESS")){
										   confiKey    =  bcpData.getVarType() +"#" + "VAR_STRESS_CONFIDENCE"    + "#" + bcpData.getCobDate();
									   }else if(bcpData.getVarType().equals("VAR_1540")){
										   confiKey    =  bcpData.getVarType() +"#" + "VAR_SIX_YEAR_CONFIDENCE"    + "#" + bcpData.getCobDate();
									   }
									   
									   String confiStatus    = globalStatus.getStatus().get(  confiKey );   
					    	    	  
									   logger.info("confiStatus key:" + confiKey + ",value:" + confiStatus + ""   );
									   if( confiStatus!=null && confiStatus.equals("error")){
										   cleanUpQueue.put( new BcpData( file.getName(),file.getPath(),"failed" ) ) ;
							    		   globalStatus.getStatus().put( bcpData.getKey(), "error");
										   alreadyProcessedFiles.add(file.getName());
									   } else if(confiStatus!=null && confiStatus.equals("success")){
										   cmdQueue.put( bcpData );
										   alreadyProcessedFiles.add(file.getName());
									   }else {
										   logger.info("holding VAR PNL since VAR Confidence level is not yet processed" + bcpData.getName());
									   }
								 } else { // immediately process confidence level var
									 cmdQueue.put( bcpData );
									 alreadyProcessedFiles.add(file.getName());
								 }
							}
				    	} else { //non var OR HYPO
				    	    if(bcpData.getExtractType().equals("HYPO") ) {
				    	    	 //check if hypo was already in staging
				    	    	 String hypoNodeKey       =  "HYPO_NODE#HYPO_NODE#" + bcpData.getCobDate();
				    	    	 String hypoNodeStatus    =  globalStatus.getStatus().get( "HYPO_NODE#HYPO_NODE#" + bcpData.getCobDate() );
				    	    	 String hypoNodeUniqueId  =  globalStatus.getStatus().get( "HYPO_NODE#UniqueIdE#" + bcpData.getCobDate() );
				    	    	 
				    	    	
				    	    	 if( hypoNodeStatus!=null) {
				    	    		 if(hypoNodeStatus.equals("success")) {
				    	    			 bcpData.sethUid(hypoNodeUniqueId);
				    	    			 cmdQueue.put( bcpData );
							    	 } else if(hypoNodeStatus.equals("error")){
				    	    			 cleanUpQueue.put( new BcpData( file.getName(),file.getPath(),"failed" ) ) ;
				    	    		 }
				    	    		
				    	    		  alreadyProcessedFiles.add(file.getName());
				    				  globalStatus.getStatus().remove(hypoNodeKey);
				    	    		  globalStatus.getStatus().remove(hypoNodeUniqueId);
				    	    	 }
				    	    	 
				    	    	 
				    	     } else { 
				    	 	   cmdQueue.put( bcpData );
				    	 	   alreadyProcessedFiles.add(file.getName());
				    	     }
				    	}
				    	
					 } else {
						    if( bcpData.getVarType().equals("VAR_1D") ||  bcpData.getVarType().equals("VAR_10D") 
						    		||  bcpData.getVarType().equals("VAR_STRESS")){
					    		if(this.gotDbError(varContainerMapping.get(bcpData.getPsrCode()),  bcpData.getCobDate())){
					    			cleanUpQueue.put( new BcpData( file.getName(),file.getPath(),"failed" ) ) ;
					    			globalStatus.getStatus().put( bcpData.getKey(), "error");
					    	    }
					    	}
						    
						    dbUtils.updateStatusInDB(bcpData, "PublishingToStagingError");
					   	 	cleanUpQueue.put( new BcpData( file.getName(),file.getPath(),"no-data" ) ) ;
					 
					        alreadyProcessedFiles.add(file.getName());
						
							
					 }
				 
				 } else {
					  logger.info("No BCP");
					  
					   String conStatus = this.getSignOffStatus( varContainerMapping.get(bcpData.getPsrCode()),  bcpData.getCobDate()  );
					   logger.info("VAR FILE at PNL POSITION LEVEL:" + bcpData.getName() + ",DbStatus:" + conStatus);
						   
					   if(conStatus!=null){
						  logger.info("Status in DB:" + conStatus + ":" + bcpData.getName() );
						   String portKey     =  bcpData.getVarType() +"#" + "VAR_PNL_PORTFOLIO" + "#" + bcpData.getCobDate();
						
						   if(bcpData.getVarType().equals("VAR_STRESS")){
							   portKey     =  bcpData.getVarType() +"#" + "VAR_STRESS_PNL_PORTFOLIO" + "#" + bcpData.getCobDate();
						   }else if(bcpData.getVarType().equals("VAR_1540")){
							   portKey     =  bcpData.getVarType() +"#" + "VAR_SIX_YEAR_PNL_PORTFOLIO" + "#" + bcpData.getCobDate();
						   }
						   
						   
						   String confiKey    =  bcpData.getVarType() +"#" + "VAR_CONFIDENCE"    + "#" + bcpData.getCobDate();
						   if(bcpData.getVarType().equals("VAR_STRESS")){
							   confiKey    =  bcpData.getVarType() +"#" + "VAR_STRESS_CONFIDENCE"    + "#" + bcpData.getCobDate();
						   } else  if(bcpData.getVarType().equals("VAR_1540")){
							   confiKey    =  bcpData.getVarType() +"#" + "VAR_SIX_YEAR_CONFIDENCE"    + "#" + bcpData.getCobDate();
						   }
						   
						   
						   String portStatus     = globalStatus.getStatus().get(  portKey );
						   String confiStatus    = globalStatus.getStatus().get(  confiKey );
						   
						   logger.info(bcpData.getName() + ":portStatus:" + portKey + "=" + portStatus + ",confiStatus:" + confiKey + "=" + confiStatus);
						   
						 	   if( (portStatus!=null && portStatus.equals("success")) && 
								   (confiStatus!=null   && confiStatus.equals("success")) &&
								   (conStatus.equals("PublishingToSubcube"))) {//just added
								  
								   cleanUpQueue.put( new BcpData( file.getName(),file.getPath()));
								   alreadyProcessedFiles.add(file.getName());
								   globalStatus.getStatus().remove(portKey);
								   globalStatus.getStatus().remove(confiKey);
							   
							   } else if(  portStatus!=null && confiStatus!=null && (portStatus.equals("error") || confiStatus.equals("error"))  ){
								  
								    cleanUpQueue.put( new BcpData( file.getName(),file.getPath(),"failed" ) ) ;
							   		alreadyProcessedFiles.add(file.getName());
							   		globalStatus.getStatus().remove(portKey);
								    globalStatus.getStatus().remove(confiKey);
								    dbUtils.rollBackVar(new BcpData( file.getName(),file.getPath(),"failed" )  );
							  
							   } else if( confiStatus!=null && ( conStatus.equals("PublishingToStagingError")
								       || conStatus.equals("PublishingToWarehouseError")
								       || conStatus.equals("PublishToWarehouseStorageError")) ){
								  
							   		cleanUpQueue.put( new BcpData( file.getName(),file.getPath(),"failed" ) ) ;
							   		alreadyProcessedFiles.add(file.getName());
							   		globalStatus.getStatus().remove(portKey);
								    globalStatus.getStatus().remove(confiKey);
								    dbUtils.rollBackVar(new BcpData( file.getName(),file.getPath(),"failed" )  );  
								    
							   } /*else if(confiStatus!=null && conStatus.equals("PublishingToSubcube")){
								   logger.info("D");
								   cleanUpQueue.put( new BcpData( file.getName(),file.getPath()));
								   alreadyProcessedFiles.add(file.getName());
									
									
								   globalStatus.getStatus().remove(portKey);
								   globalStatus.getStatus().remove(confiKey);
								   
								   
							   }else{
								   logger.info("E");
							   }*/
					
					   } else {
						   logger.info("container is not yet process:" + bcpData.getName() );
					   }
				 }
			 }
		} else {
			logger.info("File was deleted or move to archive:" + file.getName());
		}
		}catch(Exception e){
			 e.printStackTrace();
		}
	}
	
	private boolean gotDbError(String containerName, String cobDate){
		String conStatus = this.getSignOffStatus(containerName, cobDate);
		
		if(conStatus!=null && (conStatus.equals("PublishingToStagingError") 
				   || conStatus.equals("PublishingToWarehouseError") || conStatus.equals("PublishToWarehouseStorageError"))){
			return true;
		}
		return false;
	}

	public String getSignOffStatus(String containerName,String cobDate){
		
		String sql = " select top 1 status  " + 
				     " from DW.vw_SignOffAndExclude "+
				     " where containerName = '" + containerName + "' and cobDate=" + cobDate; 
		
		logger.info("Getting status in DB:" + sql);
		
		try {
			List<Map<String,Object>> status = dbUtils.executeSql(sql);
			if(status!=null && status.size()>0){
			  return (String)status.get(0).get( "status" );
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			return null;
		}
		
		return null;
	}
	
	public boolean canBcp(String fileName,int order) {
		
		String[] split = fileName.split("#");
		
		if(split.length > order){
			String exType = split[order];
			 logger.info("Extract Type:" + exType);
			 if(!exType.equals("VAR_PNL") && !exType.equals("VAR_STRESS_PNL") && !exType.equals("VAR_SIX_YEAR_PNL")   ){ //we only push  node level to warehouse and pnl to cube
				 return true;
			 } else {
				 logger.info("The File will NOT be BCP:" + exType );
				 return false;
			 }
		}
			return true;
		
	}
	
	private BcpData buildBcpData(File csvFile){
		try {
			String[] varHeaderPnl = null;
			String fileName = csvFile.getName().toString().toUpperCase();
			if( fileName.contains("P&L#PNL-VAR") || fileName.contains("PNL-VAR_STRESS") || fileName.contains("PNL-VAR_1540") ){
				fileName = csvFile.getParent() + "\\" + fileName.replace(".APX", "").split("#")[5] + "_VarRefDates.ref" ;
				if(csvFile.length()>0){
				  varHeaderPnl = FileUtils.readFileSpecificLine(fileName, ",",0);
				}
			} else {
				varHeaderPnl = new String[]{"DUMMY","DUMMY"};
			}
			
			return new BcpData( csvFile.getName().toString().toUpperCase(), "PublishingToWarehouse" ,csvFile.getAbsolutePath().toString(),varHeaderPnl );
		
		} catch (IOException e) {
			
			e.printStackTrace();
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
		
	   String fName = "V1AL0#VAR AND P&L#PNL-VAR_1D#367958375122680#20130307#367958375122680#-1#20130424#aa.APX";
	
	
		String[] split = "V1AL0#VAR AND P&L#PNL-VAR_1D#367958375122680#20130307#367958375122680#-1#20130424#aa.APX".split("#");
		int order = 8;
		if( split.length > order){
			System.out.println(split[order]);
		}else{
			System.out.println("NO DATA");
		}
		
		
		BcpCommandDirectoryFileProcessor b =new BcpCommandDirectoryFileProcessor("C:\\project\\data\\ApExtract\\", ".*.apx"); //".*2010.*\\.txt"
		System.out.println(b.canBcp(fName, 8));
		fName = "V1AL0#VAR AND P&L#PNL-VAR_1D#367958375122680#20130307#367958375122680#-1#20130424#VAR_PNL_PORTFOLIO#.APX";
		System.out.println(b.canBcp(fName, 8));
		fName = "V1AL0#VAR AND P&L#PNL-VAR_1D#367958375122680#20130307#367958375122680#-1#20130424#VAR_STRESS_PNL_PORTFOLIO#.APX";
		System.out.println(b.canBcp(fName, 8));
		
		
	
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
