package com.anz.rer.etl.csvToTable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.anz.rer.etl.directory.IFileProcessor;
import com.anz.rer.etl.directory.IFileResolver;
import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.utils.XMLUtils;

public class CsvToTableImpl implements IFileProcessor {

	private final static Logger logger = Logger.getLogger(CsvToTableImpl.class);
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

	/**
	 * if you want to override the config file againts the source file
	 * if souce file is greater than the number of column againts the config column
	 */
	private boolean configOverride;

	private DataSource dataSource;

	private IFileResolver fileResolver;

	private SortedMap<Integer, CsvConfig> csvToTableMapping = new TreeMap<Integer, CsvConfig>();

	

	public CsvToTableImpl() {
	}

	public CsvToTableImpl(DataSource dataSource, int threadPool) {
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
		logger.info("Starting process");
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
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			long endExec = System.currentTimeMillis()- startExec;
			logger.info("Total Insert time:" + endExec);
		}
		if(status.equals("No Data")){
			return false;
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
						Scanner	s = new Scanner(lineScan.next());
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
							 if( !safeCsv[0].startsWith("#") ){
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
					logger.info("Total record/line Read:"  + csvFileName + " -:-" + count);
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
					   status="No Data";
					}else{
					   status= "Data count="+ count;	
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
			CsvConfig columnConfig = entry.getValue();
			columName += columnConfig.columnName + ",";
			values += "?,";
		}

		String sqlString = " INSERT INTO " + tableName + "("
				+ columName.substring(0, columName.length() - 1) + ") VALUES("
				+ values.substring(0, values.length() - 1) + ")";
        try{
            
        	Runnable bcp = new DataExecutor(dataSource,sqlString,csvList,csvToTableMapping2, this.getBatchId());
    		
        	executor.execute(bcp);
    					 
    					 

      			    					 			 
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
       executor = Executors.newFixedThreadPool( getThreadPool());
       logger.info("bulk insert transfer done");
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

	private class CsvConfig {

		private String columnName;
		private int maxLength;
		private int columnOrder;
		private String columnType;
		private int csvColumnNumber;
		private String defValue;
		private String format;
		
		
		
		public void setColumnOrder(int columnOrder) {
			this.columnOrder = columnOrder;
		}

		public int getColumnOrder() {
			return columnOrder;
		}

		public void setMaxLength(int maxLength) {
			this.maxLength = maxLength;
		}

		public int getMaxLength() {
			return maxLength;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public String getColumnName() {
			return columnName;
		}

		public String getColumnType() {
			return columnType;
		}

		public void setColumnType(String columnType) {
			this.columnType = columnType;
		}

		public void setCsvColumnNumber(int csvColumnNumber) {
			this.csvColumnNumber = csvColumnNumber;
		}

		public int getCsvColumnNumber() {
			return csvColumnNumber;
		}

		public void setDefValue(String defValue) {
			this.defValue = defValue;
		}

		public String getDefValue() {
			return defValue;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public String getFormat() {
			return format;
		}
	}
	
	private class DataExecutor implements  Runnable {
	    private JdbcTemplate jdbc;
	    private String sqlString;
	    private List<String[]>csvList;
	    private SortedMap<Integer, CsvConfig> csvToTableMapping;
	    private DataSource ds;
	    private long batchId;
	    
		DataExecutor(DataSource dataSource, String sqlString, List<String[]>csvList,
				  SortedMap<Integer, CsvConfig> csvToTableMapping, long batchId ){
			this.ds = dataSource;
			this.sqlString = sqlString;
			this.csvList = csvList;
			this.csvToTableMapping = csvToTableMapping;
			this.batchId = batchId;
		}

		public void run() { 
			transformDataforRawPrepStmnt(csvList,  batchId);
		}
		
		
		private void transformDataforRawPrepStmnt(List<String[]>csvList,long batchId){
		    long startTime = System.currentTimeMillis();
			PreparedStatement ps = null;
			Connection conn = null;
			String sqlRawData      = null;
			try {
				 
				 conn = this.getConnection();
				 conn.setAutoCommit(false);
				 ps = conn.prepareStatement(sqlString );
	            
			List<String[]> csvs = csvList;
			
		//	logger.info("Inserting " +csvs.size()+ " record/s");
			
			for (int i = 0; i < csvs.size(); i++) { 
				
				String[] csv = csvs.get(i);
			
				for (Map.Entry<Integer, CsvConfig> entry : csvToTableMapping.entrySet() ) {
				CsvConfig columnConfig = entry.getValue(); 
					    sqlRawData      = null;
				
						 sqlRawData = csv[columnConfig.getCsvColumnNumber()];
						 if(sqlRawData!=null){
							 sqlRawData = sqlRawData.replace("null", "");
							 sqlRawData = sqlRawData.trim();
						 }
						if(sqlRawData.length() > columnConfig.getMaxLength() ) {
							if( columnConfig.getColumnType().toUpperCase().equals("SQLNUMERIC") ) {
									if(sqlRawData.length() == 0 ){
										sqlRawData = "0";
									}
									ps.setBigDecimal(columnConfig.getColumnOrder(), new BigDecimal(  sqlRawData) );
							}else if(columnConfig.getColumnType().toUpperCase().equals("CONSTANTS")){
								ps.setString(columnConfig.getColumnOrder(), columnConfig.getDefValue().substring(0, columnConfig.getMaxLength()));
							}else {	
									ps.setString(columnConfig.getColumnOrder(),	sqlRawData.substring(0, columnConfig.getMaxLength()));
							}
						} else {
							if( columnConfig.getColumnType().toUpperCase().equals("SQLNUMERIC") ){
									if(sqlRawData.length() == 0 ){
										sqlRawData = "0";
									}
									ps.setBigDecimal(columnConfig.getColumnOrder(),new BigDecimal(  sqlRawData)  );
									
									
							}else if( columnConfig.getColumnType().toUpperCase().equals("SQLFLT4")  ){
								if(sqlRawData.length() == 0 ){
									sqlRawData = "0";
								}
								ps.setFloat(columnConfig.getColumnOrder(), Float.valueOf(sqlRawData) );
						
							}else if(columnConfig.getColumnType().toUpperCase().equals("CONSTANTS")){
								ps.setString(columnConfig.getColumnOrder(), columnConfig.getDefValue());
							} else if(columnConfig.getColumnType().toUpperCase().equals("DATE")){
								  if(columnConfig.getFormat()!=null){
								   
								   String formating[] = columnConfig.getFormat().split("\\|",-1);
								   java.util.Date cob = (java.util.Date) new SimpleDateFormat(formating[0]).parse( sqlRawData );
                                   Integer cobDate = Integer.valueOf(new SimpleDateFormat( formating[1]).format(cob));
                                   ps.setString(columnConfig.getColumnOrder(), cobDate.toString());
      							       
								  } else {   	
									  ps.setString(columnConfig.getColumnOrder(), columnConfig.getDefValue());
								  }
							} else if(columnConfig.getColumnType().toUpperCase().equals("DYNAMIC")) {
								if(columnConfig.getDefValue().equalsIgnoreCase("BATCHID")){
									ps.setString(columnConfig.getColumnOrder(), String.valueOf( batchId ) );
								}
								  
							}else {	
								ps.setString(columnConfig.getColumnOrder(),	sqlRawData);
							}  
						}
				}
				 ps.addBatch();
			}
			
			logger.debug("Sql statament preparation:" + ( System.currentTimeMillis() - startTime ));
			csvList = null;
			startTime = System.currentTimeMillis();
			logger.debug("executing update........ ");
		
			   try {
					ps.executeBatch();
			   } catch (BatchUpdateException e) {
				     e.printStackTrace();
				}
			
			logger.debug("db commit");
			conn.commit();
			logger.debug("db commit end" + (System.currentTimeMillis() - startTime));
			logger.debug("Sql insert time:" + (System.currentTimeMillis() - startTime));
		
			} catch (NumberFormatException e) {
				logger.info( sqlRawData ) ;
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				logger.debug("closing resources");
				try {
					if(ps!=null)ps.close();
					
					if(conn!=null){
						conn.setAutoCommit(true);
						conn.close();
						}
					
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
				logger.debug("resources all closed");
			}
		}
		
		
		private Connection getConnection() throws SQLException {
			return ds.getConnection();
		}
		
	
	
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
			logger.info("something wrong with the filename bussDateSplitDelimeter problem");
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
