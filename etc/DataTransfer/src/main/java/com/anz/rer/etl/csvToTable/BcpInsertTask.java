package com.anz.rer.etl.csvToTable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.anz.rer.etl.utils.DbUtils;

public class BcpInsertTask implements Callable<Long>{
	
	private final static Logger logger = Logger.getLogger(BcpInsertTask.class);
	
    private BlockingQueue<BcpData> doneInsertQueue;
    private BlockingQueue<BcpData> csvDataQueue;
    private BlockingQueue<BcpData> sqlQueue;
    private BlockingQueue<BcpData> cleanUpQueue;
    
   
     
    
    private DataSource ds;
    private int batchInsert;
    private DbUtils dbUtils;
    private BcpConfig bcpConfig;
    private BcpTransactionCoordinator transCoordinator;
    
	 BcpInsertTask(BcpData bcpData,BlockingQueue<BcpData> doneQueue,DataSource dataSource, int batchInsert, BcpConfig bcpConfig,
			 BcpTransactionCoordinator transCoordinator,    
			 BlockingQueue<BcpData> sqlQueue,
			 BlockingQueue<BcpData> cleanUpQueue,
			 DbUtils dbUtils){
		 	
		 
		    doneInsertQueue = doneQueue;
		 	ds = dataSource;
		 	this.batchInsert = batchInsert;
		 	this.bcpConfig = bcpConfig;
		 	this.transCoordinator = transCoordinator;
		 	
		 	this.sqlQueue = sqlQueue;
		 	this.csvDataQueue = new ArrayBlockingQueue<BcpData>(30,true);
		 	this.cleanUpQueue = cleanUpQueue;
		 	this.dbUtils = dbUtils;
		 	
		 	try {
				csvDataQueue.put(bcpData);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	 }
	 
	 
	 @Override
	public Long call() throws Exception {
		BcpData bcpData = null;
		long startTime = System.currentTimeMillis();
		PreparedStatement prepStateMent = null;
		Connection conn = this.getConnection();
				   conn.setAutoCommit(false);
			 
		logger.debug("Insert Statement:" + bcpConfig.getBcpInsertStmnt());
			 prepStateMent = conn.prepareStatement( bcpConfig.getBcpInsertStmnt() );
			 
		 int ctr =0;
		logger.debug("Starting BcpInsertTask");
	
		try {
			while ((bcpData = csvDataQueue.take()) != null) {
				     ctr++;
				     logger.debug("Get from csvDataQueue:" + bcpData.getName() + " Done:" + bcpData.isDone());
				 	
				     if(!bcpData.isDone()){
				    	 transformDataforRawPrepStmnt(bcpData,prepStateMent,bcpConfig);
				     }
				     
				     if ((ctr) % batchInsert == 0) {
				 		 logger.debug("DB executing partial update>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
						 prepStateMent.executeBatch();
						 logger.debug("DB executing partial update DONE<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
				 	 }
				     
				 	 if(bcpData.isDone()){
 				 		logger.debug("DB executing partial update>>>>>>>>>>>" + bcpData.getName());
						prepStateMent.executeBatch();
						logger.debug("DB executing partial update DONE<<<<<<" + bcpData.getName() );
						logger.debug("DB executing COMMIT to Database<<<<<<<<" + bcpData.getName());
						conn.commit();
						logger.debug("DB executing COMMIT to Database DONE<<<<<" + bcpData.getName());
							
				 		bcpData.setStart( startTime );
					    logger.info( bcpData.totalExecutionTime( "Bcp Time for: ", System.currentTimeMillis() ) + " Status:" + bcpData.getStatus());
					    
					    bcpData.setStart( System.currentTimeMillis() );
					    bcpData.setStatus("nothing");
					    bcpData.setState(BcpConstants.HEADER);
					    doneInsertQueue.put(bcpData); 
					    bcpData = null;
					    break;
				 	 }else{
				 		bcpData = null;
				 	 }
					}
					logger.debug(">>>>>>>>>>>>>>>>>>>>>>>Exiting BcpInsertTask<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		  
		} catch (InterruptedException e) {
			e.printStackTrace();
		
		} catch(Exception e){
				logger.debug("Problem encounter....................");
				logger.debug( "It is going here: failed-bcp " );
				bcpData.setStatus("failed-bcp");	//bcperror
				dbUtils.updateStatusInDB(bcpData,"PublishingToStagingError");
				cleanUpQueue.put(bcpData);
				logger.debug(e.getLocalizedMessage());
				
				return 0l;
			
		} finally{
			
			if(prepStateMent!=null){
				prepStateMent.close();
			}
			if(conn!=null){
				conn.close();
			}
			
		}
		return 1l;
	}
	
	private Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
	
	private void transformDataforRawPrepStmnt(BcpData bcpData,PreparedStatement prepStateMent, BcpConfig bcpConfig) 
			throws Exception{
		
		try{
		
		long start = System.currentTimeMillis();
		StringBuilder sqlRawData = new StringBuilder(50);   
		List<String[]> csvs = bcpData.getCsv();
		
		for (int i = 0; i < csvs.size(); i++) { 
			String[] csv = csvs.get(i);
			String temp = "";
			for (Map.Entry<Integer, CsvConfig> entry : bcpConfig.getBcpColumnConfig().entrySet() ) {
			         CsvConfig columnConfig = entry.getValue(); 
			         
			         try{
			        	 sqlRawData.delete(0, sqlRawData.length()).append(csv[columnConfig.getCsvColumnNumber()]);
			         }catch(ArrayIndexOutOfBoundsException a){
			        	 a.printStackTrace();
			        	 logger.debug("Line: "  + i + " Getting: " + columnConfig.getCsvColumnNumber()  +  " from:" + arrToStr(csv));
			         }
			        
			       
			         if(sqlRawData!=null && (sqlRawData.equals("null")  )){
						 sqlRawData.delete(0, sqlRawData.length()).append(" ");
					 }
					 
						if(sqlRawData.length() > columnConfig.getMaxLength() ) {
							if( columnConfig.getColumnType().toUpperCase().equals("SQLNUMERIC") ) {
									if(sqlRawData.length() == 0 ){
										sqlRawData.append("0");
									}
									prepStateMent.setBigDecimal(columnConfig.getColumnOrder(),new BigDecimal(  sqlRawData.toString() )  );
							}else {	
									prepStateMent.setString(columnConfig.getColumnOrder(),	sqlRawData.substring(0, columnConfig.getMaxLength()));
							}
						
						} else {
							if( columnConfig.getColumnType().toUpperCase().equals("SQLNUMERIC") 
									|| columnConfig.getColumnType().toUpperCase().equals("SQLFLT8")  ){
									if(sqlRawData.length() == 0 ){
										sqlRawData.append("0");
									}
									
									BigDecimal bigDecimal = null;
									try{
										bigDecimal = new BigDecimal(sqlRawData.toString());
									}catch(NumberFormatException e){
										logger.debug(columnConfig.getColumnName() + ":" +sqlRawData.toString() + ":" + e.getLocalizedMessage());
										throw e;
									}
								
									prepStateMent.setBigDecimal(columnConfig.getColumnOrder(), bigDecimal );
									
							}else if( columnConfig.getColumnType().toUpperCase().equals("SQLINT")  ){
									
									if(sqlRawData.length() == 0 || sqlRawData.equals("N/A") ){
										sqlRawData.append("0");
									}
									
									Integer posId = null;
									try{
										posId = new Integer( sqlRawData.toString() ) ;
									}catch(	  NumberFormatException n){
										posId = new Integer(0);
										logger.debug(columnConfig.getColumnName() + ":" +sqlRawData.toString() + ":" + n.getLocalizedMessage());
										throw n;
										
									}
									
									prepStateMent.setInt(columnConfig.getColumnOrder(),posId );
									
							}else {	
								prepStateMent.setString(columnConfig.getColumnOrder(),	sqlRawData.toString() );
							}  
						}
						
						sqlRawData.delete(0, sqlRawData.length());
			}
			 temp="";
			 prepStateMent.addBatch();
		}
		
		
		logger.debug("Sql Construct Time:" + (System.currentTimeMillis() - start));
		}catch(java.lang.ArrayIndexOutOfBoundsException a ){
			a.printStackTrace();
			throw a;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	
	private String arrToStr(String[] arr){
		String ret = "";
		 ret = arr[0];
		for (int i = 1; i < arr.length; i++) {
			ret+="," + arr[i] ;
		}
		
		return ret;
	}
	
	public BlockingQueue<BcpData> getCsvDataQueue() {
		return csvDataQueue;
	}


	public void setCsvDataQueue(BlockingQueue<BcpData> csvDataQueue) {
		this.csvDataQueue = csvDataQueue;
	}
	
	 public static void main(String args[]){
		 
		 
		 
		 
		 
	
		 
		 
		 
		 
		 
		 
		 
		 
		 
		 
		 
	 }
	 
	 
	
}