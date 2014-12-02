/*package com.anz.rer.etl.csvToTable;

import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

public class BcpInsert implements Runnable{
   
	private final static Logger logger = Logger.getLogger(BcpInsert.class);
	
	private BlockingQueue<BcpData> sqlQueue;
	private BlockingQueue<BcpData> doneQueue;
	
	private ExecutorService bcpInsert;

	
	private BcpTransactionCoordinator bcpTranCoordinator;
	private JdbcTemplate jdbc;
	private DataSource dataSource;
	private int numberOfThread;
	private int batchInsert;
	
	
	public BcpInsert(BlockingQueue<BcpData> sqlQueue,
			BlockingQueue<BcpData> doneQueue,
			BcpTransactionCoordinator bcpTranCoordinator, 
			DataSource dataSource, int numberOfThread,
			int batchInsert
			) {
		super();
		this.sqlQueue = sqlQueue;
		this.doneQueue = doneQueue;
		this.bcpTranCoordinator = bcpTranCoordinator;
		
		this.dataSource = dataSource;
		this.batchInsert = batchInsert;
		bcpInsert =  Executors.newFixedThreadPool( numberOfThread);
	}

	public void run() {
	 	while(true){
		   BcpData bcpData = sqlQueue.poll();
		   if(null != bcpData){
			  logger.debug("picked up in queue:" + bcpData.getName() + ":" + bcpData.getId());
			   bcpInsert.execute(new BcpInsertTask( bcpData, doneQueue, dataSource, batchInsert ));
		   }
		}
	}
	
	  
	public void setNumberOfThread(int numberOfThread) {
		this.numberOfThread = numberOfThread;
	}

	public int getNumberOfThread() {
		return numberOfThread;
	}




	class BcpInsertTask implements Runnable{
         private BcpData bxpData;
         private BlockingQueue<BcpData> doneInsertQueue;
         private DataSource ds;
         private int batchInsert;
         
		 BcpInsertTask(BcpData bcpData,BlockingQueue<BcpData> doneQueue,DataSource dataSource, int batchInsert){
			 	bxpData = bcpData;
			 	doneInsertQueue = doneQueue;
			 	ds = dataSource;
			 	this.batchInsert = batchInsert;
		 }
		 
		@Override
		public void run() {
			transformDataforRawPrepStmnt(bxpData);
		}
		
		private Connection getConnection() throws SQLException {
			return ds.getConnection();
		}
		
		private void transformDataforRawPrepStmnt(BcpData bxpData){
		  	
			long startTime = System.currentTimeMillis();
			   
			PreparedStatement ps = null;
			Connection conn = null;
			StringBuilder sqlRawData = new StringBuilder(50);
			try {
				 logger.debug( bxpData.getBcpConfig().getBcpInsertStmnt() );
				 conn = this.getConnection();
				 conn.setAutoCommit(false);
				 ps = conn.prepareStatement( bxpData.getBcpConfig().getBcpInsertStmnt() );
	            
			List<String[]> csvs = bxpData.getCsv();
			for (int i = 0; i < csvs.size(); i++) { 
				String[] csv = csvs.get(i);
				for (Map.Entry<Integer, CsvConfig> entry : bxpData.getBcpConfig().getBcpColumnConfig().entrySet() ) {
				CsvConfig columnConfig = entry.getValue(); 
					     
						 sqlRawData.append(csv[columnConfig.getCsvColumnNumber()]);
						 
						 if(sqlRawData!=null && sqlRawData.equals("null") ){
							 sqlRawData.delete(0, sqlRawData.length()).append(" ");
						 }
						if(sqlRawData.length() > columnConfig.getMaxLength() ) {
							if( columnConfig.getColumnType().toUpperCase().equals("SQLNUMERIC") ) {
									if(sqlRawData.length() == 0 ){
										sqlRawData.append("0");
									}
									ps.setBigDecimal(columnConfig.getColumnOrder(),new BigDecimal(  sqlRawData.toString() )  );
							}else {	
									ps.setString(columnConfig.getColumnOrder(),	sqlRawData.substring(0, columnConfig.getMaxLength()));
							}
						} else {
							if( columnConfig.getColumnType().toUpperCase().equals("SQLNUMERIC") ){
									if(sqlRawData.length() == 0 ){
										sqlRawData.append("0");
									}
									ps.setBigDecimal(columnConfig.getColumnOrder(),new BigDecimal( sqlRawData.toString() )  );
							}else {	
								ps.setString(columnConfig.getColumnOrder(),	sqlRawData.toString() );
							}  
						}
						sqlRawData.delete(0, sqlRawData.length());
				}
				 ps.addBatch();
				 if ((i + 1) % batchInsert == 0) {
					 ps.executeBatch();
				  }
			}
			
			logger.debug("Sql statament preparation:" + ( System.currentTimeMillis() - startTime ));
			
			startTime = System.currentTimeMillis();
			 
			logger.debug("executing update........ ");
				try {
					if (bxpData.getStatus() != null	&& bxpData.getStatus().equals("failed")) {
						logger.info("retrying to execute again the sp ");
					}
					
					ps.executeBatch();
				} catch (BatchUpdateException e) {
					e.printStackTrace();
					bxpData.setStatus("failed-bcp");
				}  catch (Exception e) {
					bxpData.setStatus("failed-bcp");
					e.printStackTrace();
				}
			
			logger.debug("db commit start");
			conn.commit();
			logger.debug("db commit end:" + (System.currentTimeMillis() - startTime));
			logger.debug("Sql insert time:" + (System.currentTimeMillis() - startTime));
			logger.debug( "Done insert to table:" + bxpData.getName() + ":" + bxpData.getId() );
	
			   if(bcpTranCoordinator.transactionDone(bxpData)){
				   bxpData.setStart( System.currentTimeMillis() );
				   logger.info( bxpData.totalExecutionTime( "Bcp Time for: ", System.currentTimeMillis() ) + " Status:" + bxpData.getStatus());
				   bxpData.setStart( System.currentTimeMillis() );
				   bxpData.setStatus("nothing");
				   bxpData.setState(BcpConstants.HEADER);
				   doneInsertQueue.put(bxpData);   
			   } else {
				   bxpData = null;
			   }
		
			} catch (NumberFormatException e) {
				logger.debug( sqlRawData ) ;
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
		
		
	
		
		
		
		 
	 }
		
	}
	

	
	*/