package com.anz.rer.etl.csvloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;

import com.anz.rer.etl.directory.IFileProcessor;
import com.anz.rer.etl.directory.IFileResolver;
import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.utils.XMLUtils;

public class CsvLoaderImpl implements IFileProcessor {

	private final static Logger logger = Logger.getLogger(CsvLoaderImpl.class);
	protected ExecutorService executor; 
	
	private int insertThreshold;
	
	private String delimeter;
	private String rowDelimeter;
	private String xmlFormatFile;
	private String tableName;
	private boolean loadPreviousDateData;
	private boolean skipFirstLine;
	private int threadPool;  
	private StringBuilder sData;
	private int csvSourceLength;
	
	private String currentFileName;
	private DbUtils dbUtils;
	private int bussDate;
	private String bussDateSplitDelimeter = "_";
	
	
	private long batchId;
	private String status;
	private List<Future<Boolean>> csvLoaderTask = new ArrayList<Future<Boolean>>();
	
	/**
	 * if you want to override the config file againts the source file
	 * if souce file is greater than the number of column againts the config column
	 */
	private boolean configOverride;

	private DataSource dataSource;

	private IFileResolver fileResolver;

	private SortedMap<Integer, CsvConfig> csvToTableMapping = new TreeMap<Integer, CsvConfig>();

	

	public CsvLoaderImpl() {
	}

	public CsvLoaderImpl(DataSource dataSource, int threadPool) {
		this.dataSource = dataSource;
		
		if( threadPool == 0 ) {
			setThreadPool( Runtime.getRuntime().availableProcessors() );
		} else {
			setThreadPool( threadPool );
		}
		executor = Executors.newFixedThreadPool( getThreadPool() );
		
	}

	public void init(Properties properties) {
	//	logger.info(properties.get("anz.etl.csv.source.file.pattern"));
	}

	public boolean doEtlTransfer() {
		long startExec = System.currentTimeMillis();
		String csvFileName;
		logger.info("Starting deleting table Staging.MRE_Position");
		
		String delSql = " delete from " + getTableName() ;
		try {
			this.getDbUtils().execute(delSql);
		} catch (SQLException e) {
			logger.error("Failed when delete data from Staging table " + delSql + " due to: " +e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("Starting insert process");
		try {
			csvFileName = fileResolver.getSrcFileName(); 
			if (csvFileName != null) {
				this.loadCsv(csvFileName);
			} else {
				logger.info(" Source file not available for :" + csvFileName);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
//			executor.shutdown();
//			while (!executor.isTerminated()) {
//			}
//			
			logger.info("doEtlTransfer status:" + status);
			if(status!=null && status.equals("Complete - No Data")){
				return false;
			}
			
			for (Future<Boolean> future : csvLoaderTask) {
     			try {
     				Boolean result = future.get();
     				if(result!=null ){
     					//logger.info(result.booleanValue());
     				   if(!result.booleanValue()){
     					  status="Failed"; 
     					  logger.info("Insert encounter Exception Setting csvLoaderTask to null");
     	     			  csvLoaderTask = null;	
     	     			
     	     			  csvLoaderTask = new ArrayList<Future<Boolean>>();
     	     			  logger.info("Setting csvLoaderTask to new Instance");
     					  return false;   
     				   }	 
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				} catch (ExecutionException e) {
					e.printStackTrace();
					return false;
				} catch (Exception e){
					e.printStackTrace();
				    return false;
				}
     		}
			logger.info("setting csvLoaderTask to null");
 			csvLoaderTask = null;	
 			csvLoaderTask = new ArrayList<Future<Boolean>>();
 			logger.info("setting csvLoaderTask to new count");
			
			
			long endExec = System.currentTimeMillis()- startExec;
			logger.info("Total Insert time:" + endExec);
			
		}
		
		return true;
	}
   
	  
	
	protected void loadCsv(String csvFileName) {
		
		String[] safeCsv = new String[csvSourceLength];
		Scanner lineScan = null; 
		boolean skipLine = this.isSkipFirstLine();
		try {
			
		   Pattern rowDelim = Pattern.compile(rowDelimeter);
		   Pattern colDelim = Pattern.compile(delimeter);
		 
		   lineScan = new Scanner( new FileReader(csvFileName) );
			if (lineScan != null) {
				lineScan.useDelimiter(rowDelim);
					int ctr = 0; 
					int count = 0;
					List<String[]> csvLines = new ArrayList<String[]>();
					
					while (lineScan.hasNext()) {
						Scanner	s = new Scanner(" " + lineScan.next() + " " );
						
						s.useDelimiter(colDelim);
						int ctr1=0;
						safeCsv = new String[csvSourceLength];
						while (s.hasNext()) {
							if(ctr1<safeCsv.length){
								safeCsv[ctr1]= s.next().trim();
							} else {
								s.next();
							}
							ctr1++;
						}
						
						if(skipLine){
							skipLine=false; 
						} else {
							 if( !safeCsv[0].startsWith("#") && !safeCsv[0].startsWith(" #")  ){
								 if (ctr ==  insertThreshold) {
									 
									csvLines.add(extractData(safeCsv)); 
									
									insertBatch(csvLines, csvToTableMapping, tableName);
									csvLines = null;
									csvLines = new ArrayList<String[]>();
									ctr = -1;
								} else {
									csvLines.add(extractData(safeCsv));
								}
							 }
							 ctr++;
							 count++;
						}
					}
					logger.info("Total record/line Read:"  + csvFileName + " :" + count + " Status=" + status);
					if (!csvLines.isEmpty()) {
						insertBatch(csvLines, csvToTableMapping, tableName);
					}
					
					if(csvLines!=null && csvLines.size()>0){
						if(arrayToString(csvLines.get(0), this.delimeter) !=null ){
					      setsData(new StringBuilder( this.strArrToStringBuilder( csvLines.get(0), this.delimeter) )  );
						}
					}
					csvLines = null;
					if(count==0){
					   status="Complete - No Data";
					} else {
						status=null;
					}
					
					
			}
		} catch (FileNotFoundException e) {
			logger.error("Source data file not found: " + csvFileName);
			e.printStackTrace();
		} catch (NullPointerException e) {
			logger.error("File is not presented for date: " + csvFileName);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lineScan.close();
			
		}
	}

	
	protected boolean doBatchInsert(List<String[]> csvs) {
	
		try {
			  int ctr = 0;
			  int count = 0;
			  List<String[]> csvLines = new ArrayList<String[]>();
			  for (String[] csv : csvs) {
								 if (ctr ==  insertThreshold ) {
									csvLines.add(extractData(csv)); 
									insertBatch(csvLines, csvToTableMapping, tableName);
									csvLines = null;
									csvLines = new ArrayList<String[]>();
									ctr = -1;
								} else {
									csvLines.add(extractData(csv));
								}
							 ctr++;
			   }
					 logger.info("Total Line Read:" + count);
					if (!csvLines.isEmpty()) {
						insertBatch(csvLines, csvToTableMapping, tableName);
					}
					csvLines = null;
			  return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


	
	private void insertBatch(final List<String[]> csvList,
			final SortedMap<Integer, CsvConfig> csvToTableMapping2,
			final String tableName) {
		
		String columName = "";
		String values = "";

		for (Map.Entry<Integer, CsvConfig> entry : csvToTableMapping2.entrySet()) {
			columName += entry.getValue().getColumnName() + ",";
			values += "?,";
		}

		String sqlString = " INSERT INTO " + tableName + "("
				+ columName.substring(0, columName.length() - 1) + ") VALUES("
				+ values.substring(0, values.length() - 1) + ")";
        try{
            
        	Callable<Boolean> csvTask = new  CsvLoaderTask(dataSource,sqlString,csvList,csvToTableMapping2, this.getBatchId());
        	     Future<Boolean> task = executor.submit(csvTask);
        	     csvLoaderTask.add(task);
        } catch(Exception e){
        	e.printStackTrace();
        }
	}

	private String[] extractData(String[] csvData) {
		String[] data = new String[csvSourceLength];
		for (int i = 0; i < data.length; i++) {
			  try{
				  if(csvData[i]==null){
					  csvData[i]="";
				  }	
				  data[i] = csvData[i];
				} catch(ArrayIndexOutOfBoundsException arrIndex){
					logger.info("The source data length of " + csvData.length + " is not equal to config length of " + csvToTableMapping.size());
					logger.error("The source data length of " + csvData.length + " is not equal to config length of " + csvToTableMapping.size());
				}
		}
		
		return data;
	}

	public boolean loadSourceXmlConfigFile(String fileName) {
		
		try {
			logger.info("loading xml config file:" + fileName);
			File xmlFile = new File(fileName);
			Document xmlConfig = XMLUtils.parse(xmlFile);
			XMLUtils.removeAllNamespaces(xmlConfig);
			List list = xmlConfig.selectNodes("//RECORD/FIELD");
			
			csvToTableMapping = null;
			csvToTableMapping = new TreeMap<Integer, CsvConfig>();
			
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Node field = (Node) iter.next();
				
				String id = field.valueOf("@ID");
				String xpathStr = "//ROW/COLUMN[@SOURCE='" + id + "']";
				
				Node column = field.selectSingleNode(xpathStr);
				
				CsvConfig csvConfig = new CsvConfig();
						  csvConfig.setColumnName(column.valueOf("@NAME"));
						  csvConfig.setColumnOrder(Integer.valueOf(id));
						  csvConfig.setMaxLength(Integer.valueOf(field.valueOf("@MAX_LENGTH")));
						  csvConfig.setColumnType(column.valueOf("@xsi:type"));
						  csvConfig.setCsvColumnNumber(Integer.valueOf(column.valueOf("@CSVSOURCE")));
						  csvConfig.setDefValue( column.valueOf("@VALUE") );
						  csvConfig.setFormat( column.valueOf("@FORMAT") );
						  
						  
						  
				csvToTableMapping.put(csvConfig.getColumnOrder(), csvConfig);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
			return false;
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
        return true;
	}

	public String getDelimeter() {
		return delimeter;
	}

	public void setDelimeter(String delimeter) {
		this.delimeter = delimeter;
	}

	public int getInsertThreshold() {
		return insertThreshold;
	}

	public void setInsertThreshold(int insertThreshold) {
		this.insertThreshold = insertThreshold;
	}

	public Map<Integer, CsvConfig> getCsvToTableMapping() {
		return csvToTableMapping;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setXmlFormatFile(String xmlFormatFile) {
		this.xmlFormatFile = xmlFormatFile;
	}

	public String getXmlFormatFile() {
		return xmlFormatFile;
	}

	public boolean isLoadPreviousDateData() {
		return loadPreviousDateData;
	}

	public void setLoadPreviousDateData(boolean loadPreviousDateData) {
		this.loadPreviousDateData = loadPreviousDateData;
	}

	public boolean isSkipFirstLine() {
		return skipFirstLine;
	}

	public void setSkipFirstLine(boolean skipFirstLine) {
		this.skipFirstLine = skipFirstLine;
	}

	
	public void setFileResolver(IFileResolver fileResolver) {
		this.fileResolver = fileResolver;
	}

	
	
	public IFileResolver getFileResolver() {
		return fileResolver;
	}

	@Override
	public boolean preProcess() {
		/**
		 * loading the config
		 */
		String csvFileName;
	
		logger.info("Starting pre process");
		
		if(csvSourceLength<=0){
			 logger.info("Please check Length of CSV source data configuration");
			 return false;
		}
		 
		try {
			csvFileName = fileResolver.getSrcFileName(); 
		
			if (csvFileName != null) {
				logger.info(" Source file will be loaded from data:"	+ csvFileName);
				this.loadSourceXmlConfigFile(fileResolver.getSrcConfigFileName());
				logger.info("loading xml config file done...");
				
				
				/*if ( this.loadSourceXmlConfigFile(fileResolver.getSrcConfigFileName()) ) {
					String[] header = FileUtils.readFileSpecificLine( csvFileName, delimeter,0 );
					if( csvToTableMapping.size()!= header.length && !configOverride ) {
					  logger.info("The source file column size dont match with the import config file");
					  logger.info("program will now exit");
					  return false;
					}
				}*/
				
			} else {
				logger.info(" Source file not available for :" + csvFileName);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	

	@Override
	public boolean doProcess() {
	   logger.info("executing bulk insert transfer");			
	   boolean retVal =  this.doEtlTransfer();
       if(retVal){
    	   executor = Executors.newFixedThreadPool( getThreadPool());
    	   logger.info("bulk insert transfer done");
       } 
       
	   return retVal; 
       
	}

	@Override
	public boolean postProcess() {
		return true;
	}

	@Override
	public boolean validate(File fileName) {
		logger.debug("validating filename:" + fileName.getName() );
	 
		fileResolver.setFile(fileName);
		fileResolver.resolveSrcFileName();
	 	return true;
	}
   
	public void setThreadPool(int threadPool) {
		this.threadPool = threadPool;
	}

	public int getThreadPool() {
		return threadPool;
	}

	protected ExecutorService getExecutor() {
		return executor;
	}

	protected void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public boolean isConfigOverride() {
		return configOverride;
	}

	public void setConfigOverride(boolean configOverride) {
		this.configOverride = configOverride;
	}

	public String getRowDelimeter() {
		return rowDelimeter;
	}

	public void setRowDelimeter(String rowDelimeter) {
		this.rowDelimeter = rowDelimeter;
	}
	
	private StringBuilder strArrToStringBuilder(String[] strArr, String delimeter){
		if(strArr!=null && strArr.length>0){
			StringBuilder ret = new StringBuilder(strArr[0]);
			
			for (int i = 1; i < strArr.length; i++) {
			  ret.append(this.delimeter);
			  ret.append(strArr[i]);
			}
			return ret;
		} else {
			return null;
		}
	}
	
	private String arrayToString(String[] a, String separator) {
	    StringBuffer result = new StringBuffer();
	     boolean valid = false;
	    if (a.length > 0) {
	        if(a[0]!=null && a[0].trim().length()>0 ){
	    	 result.append(a[0]);
	    	 valid = true;
	        }
	        for (int i=1; i<a.length; i++) {
	        	result.append(separator);
	        	if(a[i]!=null && a[i].trim().length()>0 ){
	    	    	result.append(a[i]);
	        		valid = true;
	        	}
	        }
	    }
	    if(valid){
	     return result.toString();
	    }else {
	     return	null;
	    }
	}
	public void setsData(StringBuilder sData) {
		this.sData = sData;
	}

	public StringBuilder getsData() {
		return sData;
	}

	public void setCsvSourceLength(int csvSourceLength) {
		this.csvSourceLength = csvSourceLength;
	}

	public int getCsvSourceLength() {
		return csvSourceLength;
	}

	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}

	public String getCurrentFileName() {
		return currentFileName;
	}

	public void setDbUtils(DbUtils dbUtils) {
		this.dbUtils = dbUtils;
	}

	public DbUtils getDbUtils() {
		return dbUtils;
	}

	public void setBussDate(int bussDate) {
		this.bussDate = bussDate;
	}

	public int getBussDate() {
		return bussDate;
	}

 
	@SuppressWarnings("unused")
	protected String getBussNameFromFileName(String fileName, String fileNamePadStr, String fileExtension, int dateLocation){
		try{
		
		return fileName.split(bussDateSplitDelimeter)[dateLocation];
		} catch(Exception e){
			logger.info("something wrong with the filename: bussDateSplitDelimeter problem" );
			e.printStackTrace();
		}
		return fileName;
		  
	 
	}

	
	public String getBussDateSplitDelimeter() {
		return bussDateSplitDelimeter;
	}

	public void setBussDateSplitDelimeter(String bussDateSplitDelimeter) {
		this.bussDateSplitDelimeter = bussDateSplitDelimeter;
	}

	public void setBatchId(long batchId) {
		this.batchId = batchId;
	}

	public long getBatchId() {
		return batchId;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	
	
}
