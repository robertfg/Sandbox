package com.anz.rer.etl.csvToTable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class BcpInsertTaskOrig implements Callable<Long>{
	
	private final static Logger logger = Logger.getLogger(BcpInsertTaskOrig.class);
	
    private BlockingQueue<BcpData> doneInsertQueue;
    private BlockingQueue<BcpData> csvDataQueue;
    private BlockingQueue<BcpData> sqlQueue;
    
    
    
    private DataSource ds;
    private int batchInsert;
   
    private BcpConfig bcpConfig;
  //  private ExecutorService executorService;
    
   // private List<Future<Long>> batchReply = new ArrayList<Future<Long>>();
    
	 BcpInsertTaskOrig(BcpData bcpData,BlockingQueue<BcpData> doneQueue,DataSource dataSource, int batchInsert, BcpConfig bcpConfig,
			 /*BcpTransactionCoordinator transCoordinator,  */  BlockingQueue<BcpData> sqlQueue){
		 	doneInsertQueue = doneQueue;
		 	ds = dataSource;
		 	this.batchInsert = batchInsert;
		 	this.bcpConfig = bcpConfig;
		 	/*this.transCoordinator = transCoordinator;*/
		 	this.sqlQueue = sqlQueue;
		 	this.csvDataQueue = new ArrayBlockingQueue<BcpData>(30,true);
		 	try {
				csvDataQueue.put(bcpData);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
	/*	 	executorService = Executors.newFixedThreadPool(10,new ThreadFactory() {
				AtomicInteger threadCtr = new AtomicInteger();
				@Override
				public Thread newThread(Runnable r) {
					 Thread t = new Thread(r,"BcpInsertTak-" + threadCtr.incrementAndGet());
					 return t;
				}
			});
	*/	 	
	 }
	 
	 
	 @Override
	public Long call() throws Exception {
		BcpData bcpData;
		long startTime = System.currentTimeMillis();
		PreparedStatement prepStateMent = null;
		Connection conn = null;
			 conn = this.getConnection();
			 conn.setAutoCommit(false);
			 prepStateMent = conn.prepareStatement( bcpConfig.getBcpInsertStmnt() );
			 int ctr =0;
		logger.debug("Started BcpInsertTask");
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
				 		/*if(prepStateMent!=null){
					 		   Callable<Long> batchTask = new BatchExecute(prepStateMent);
					 		   batchReply.add( executorService.submit(batchTask));
			        	}*/
				  	   logger.debug("DB executing partial update DONE<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
					 		
				 	 }
				     
				 	 if(bcpData.isDone()){
				 		logger.debug("DB executing partial update>>>>>>>>>>>" + bcpData.getName());
				 		prepStateMent.executeBatch();
				 		
				 		  
				 		/*   for (Future<Long> reply : batchReply) {
							logger.debug("Update time:" + reply.get());
						   }
				 		    Callable<Long> batchCommitTask = new BatchCommit(prepStateMent,conn);
					 		Future<Long> commitReply =  executorService.submit(batchCommitTask);*/
			        	  //  logger.debug("Commit time:" + commitReply.get() );
			        						  
				 		logger.debug("DB executing partial update DONE<<<<<<" + bcpData.getName() );
						logger.debug("DB executing COMMIT to Database<<<<<<<<" + bcpData.getName());
						//conn.commit();
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
			e.printStackTrace();
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
	
	private void transformDataforRawPrepStmnt(BcpData bcpData,PreparedStatement prepStateMent, BcpConfig bcpConfig) throws Exception{
		try{
		long start = System.currentTimeMillis();
		StringBuilder sqlRawData = new StringBuilder(50);   
		List<String[]> csvs = bcpData.getCsv();
		for (int i = 0; i < csvs.size(); i++) { 
			String[] csv = csvs.get(i);
			for (Map.Entry<Integer, CsvConfig> entry : bcpConfig.getBcpColumnConfig().entrySet() ) {
			         CsvConfig columnConfig = entry.getValue(); 
			         try{
					 sqlRawData.append(csv[columnConfig.getCsvColumnNumber()]);
			         }catch(ArrayIndexOutOfBoundsException a){
			        	 a.printStackTrace();
			        	 logger.debug("Getting: " + columnConfig.getCsvColumnNumber()  +  " from:" + csv.toString());
			         }
					 if(sqlRawData!=null && sqlRawData.equals("null") ){
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
						if( columnConfig.getColumnType().toUpperCase().equals("SQLNUMERIC") ){
								if(sqlRawData.length() == 0 ){
									sqlRawData.append("0");
								}
								prepStateMent.setBigDecimal(columnConfig.getColumnOrder(),new BigDecimal( sqlRawData.toString() )  );
						}else {	
							prepStateMent.setString(columnConfig.getColumnOrder(),	sqlRawData.toString() );
						}  
					}
					sqlRawData.delete(0, sqlRawData.length());
			}
			 prepStateMent.addBatch();
		}
		logger.debug("Sql Construct Time:" + (System.currentTimeMillis() - start));
		}catch(java.lang.ArrayIndexOutOfBoundsException a ){
			a.printStackTrace();
		}catch(Exception e){
			
		}
	}
			
	
	public BlockingQueue<BcpData> getCsvDataQueue() {
		return csvDataQueue;
	}


	public void setCsvDataQueue(BlockingQueue<BcpData> csvDataQueue) {
		this.csvDataQueue = csvDataQueue;
	}

	
	class BatchExecute implements Callable<Long> {
		    PreparedStatement prepStateMent;
		
		   public BatchExecute(PreparedStatement prepStateMent){
			   this.prepStateMent = prepStateMent;
		   }
		
			@Override
			public Long call() throws Exception {
				long start = System.currentTimeMillis();
				try {
					prepStateMent.executeBatch();
				} catch (SQLException e) {
					
					e.printStackTrace();
				} catch(Exception e){
					e.printStackTrace();
				}
				
				return System.currentTimeMillis() - start;
			}
		}
	
	class BatchCommit implements Callable<Long> {
	    PreparedStatement prepStateMent;
	    Connection conn;
	
	   public BatchCommit(PreparedStatement prepStateMent, Connection conn){
		   this.prepStateMent = prepStateMent;
		   this.conn = conn;
	   }
		
		@Override
		public Long call() throws Exception {
			long start = System.currentTimeMillis();
			 	try {
			 		
			 		if(prepStateMent!=null){
			 			prepStateMent.executeBatch();
			 		}
			 		
					conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				} catch(Exception e){
					e.printStackTrace();
				}
			 
				return System.currentTimeMillis() - start;
		}
	}
	
}