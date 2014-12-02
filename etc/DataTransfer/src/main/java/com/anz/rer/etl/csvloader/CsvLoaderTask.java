package com.anz.rer.etl.csvloader;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.apache.log4j.Logger;



public class CsvLoaderTask implements Callable<Boolean> {

	private final static Logger logger = Logger.getLogger(CsvLoaderTask.class);
	
	@Override
	public Boolean call() throws Exception {
		//http://kamleshkr.wordpress.com/2009/10/02/java-threads-callable-and-future/
		//http://whitelassiblog.wordpress.com/2011/09/25/java-concurrency-utilities-part-01-callable-future-and-futuretask/
		return transformDataforRawPrepStmnt(csvList,  batchId);
	}
	
	
	private String sqlString;
    private List<String[]>csvList;
    private SortedMap<Integer, CsvConfig> csvToTableMapping;
    private DataSource ds;
    private long batchId;
    
    CsvLoaderTask(DataSource dataSource, String sqlString, List<String[]>csvList,
			  SortedMap<Integer, CsvConfig> csvToTableMapping, long batchId ){
		this.ds = dataSource;
		this.sqlString = sqlString;
		this.csvList = csvList;
		this.csvToTableMapping = csvToTableMapping;
		this.batchId = batchId;
	}

	
	
	
	private boolean transformDataforRawPrepStmnt(List<String[]>csvList,long batchId){
	    long startTime = System.currentTimeMillis();
		PreparedStatement ps = null;
		Connection conn = null;
		String sqlRawData      = null;
		String[] csv = null;
		try { 
			 
			 conn = this.getConnection();
			 conn.setAutoCommit(false);
			 ps = conn.prepareStatement(sqlString );
			 logger.debug("SQLString:" + sqlString);
            
		List<String[]> csvs = csvList;
		 
		for (int i = 0; i < csvs.size(); i++) { 
			
			 csv = csvs.get(i);
		
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
		ps.executeBatch();
		logger.debug("db commit");
		conn.commit();
		logger.debug("db commit end" + (System.currentTimeMillis() - startTime));
		logger.debug("Sql insert time:" + (System.currentTimeMillis() - startTime));
	    return true;
		} catch (NumberFormatException e) {
			logger.info( "SQL RAW DATA THAT CAUSED PROBLEM:" + sqlRawData ) ;
			logger.info(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.info(e.getMessage());
			
			for(int x = 0; x<csv.length;x++){
			  logger.info(  strArrToStringBuilder( csv ) );
			}
			
			
			e.printStackTrace();  
		} finally{
			//logger.info("closing resources");
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
		return false;
	}
	
	
	private Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	private StringBuilder strArrToStringBuilder(String[] strArr){
		if(strArr!=null && strArr.length>0){
			StringBuilder ret = new StringBuilder(strArr[0]);
			
			for (int i = 1; i < strArr.length; i++) {
			  ret.append(";");
			  ret.append(strArr[i]);
			}
			return ret;
		} else {
			return null;
		}
	}

}
