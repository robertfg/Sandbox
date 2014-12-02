package com.anz.rer.etl.csvToTable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.anz.rer.etl.utils.DbUtils;

public class BcpInsertPool implements Runnable{
   
	private final static Logger logger = Logger.getLogger(BcpInsertPool.class);
	private BlockingQueue<BcpData> sqlQueue;
	private BlockingQueue<BcpData> doneQueue;
	private ExecutorService bcpInsertPool;
	private BcpTransactionCoordinator bcpTranCoordinator;
	private DataSource dataSource;
	private int numberOfThread;
	private int batchInsert;
	
	
	private Map<String,BcpConfig> bcpConfig = new HashMap<String,BcpConfig>();
	private BlockingQueue<BcpData> cleanUpQueue;
   
	private DbUtils dbUtils;
	
	private ConcurrentMap<String,BcpInsertTask> threadMgr = new ConcurrentHashMap<String,BcpInsertTask>();

	public BcpInsertPool(BlockingQueue<BcpData> sqlQueue,
			BlockingQueue<BcpData> doneQueue,
			BcpTransactionCoordinator bcpTranCoordinator, 
			DataSource dataSource, int numberOfThread,
			int batchInsert, Map<String,BcpConfig> bcpConfig,
			BlockingQueue<BcpData> cleanUpQueue,DbUtils dbUtils
	
			) {
		super();
		this.sqlQueue = sqlQueue;
		this.doneQueue = doneQueue;
		this.bcpTranCoordinator = bcpTranCoordinator;
		
		
		this.dataSource   =  dataSource;
		this.batchInsert  =  batchInsert;
		this.bcpConfig    =  bcpConfig;
		bcpInsertPool     =  Executors.newFixedThreadPool( numberOfThread);
		this.cleanUpQueue =  cleanUpQueue;
		
		this.dbUtils = dbUtils;
		
		
	}

	public void run() {
	 	BcpData bcpData;
		BcpInsertTask bcpInsertTaskThread = null;
		try {
			while ((bcpData = sqlQueue.take()) != null) {
				logger.debug("getting from sqlQueue:" + bcpData.getName() + " :" + bcpData.getId() + " Done:" + bcpData.isDone());
				try {
					bcpInsertTaskThread = threadMgr.get(bcpData.getName());
					if(null!=bcpInsertTaskThread){
						if(bcpData.isDone()) {
							bcpInsertTaskThread.getCsvDataQueue().put(bcpData);
							threadMgr.remove(bcpData.getName());
						} else {
							bcpInsertTaskThread.getCsvDataQueue().put(bcpData);
						}
					} else {
						  logger.debug("Creating new Task:" + bcpData.getName());
						  bcpInsertTaskThread =  new BcpInsertTask( bcpData,doneQueue,dataSource,batchInsert,getBcpConfig(bcpData.getName()),bcpTranCoordinator, sqlQueue,cleanUpQueue,dbUtils     );
						  threadMgr.put(bcpData.getName(), bcpInsertTaskThread);
						  bcpInsertPool.submit( bcpInsertTaskThread ); 
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> BcpInsertPool Exiting <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		} catch (InterruptedException e) {
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
	
	  
	public void setNumberOfThread(int numberOfThread) {
		this.numberOfThread = numberOfThread;
	}

	public int getNumberOfThread() {
		return numberOfThread;
	}




	
		
	}
	

	
	