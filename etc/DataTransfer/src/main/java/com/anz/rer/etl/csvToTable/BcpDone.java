package com.anz.rer.etl.csvToTable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.utils.GlobalStatusUtil;

public class BcpDone implements Runnable{
	
	private final static Logger logger = Logger.getLogger(BcpDone.class);
	private BlockingQueue<BcpData> doneQueue;
	private BlockingQueue<BcpData> cleanUpQueue;
	private DbUtils dbUtils;
	private int numberOfThread;
	protected ExecutorService doneExecutor;
	private boolean insertToHeader;
	private boolean insertToFact;
	private int retryThreshold;
	private GlobalStatusUtil globalStatus;
	
	
	
	public BcpDone(BlockingQueue<BcpData> doneQueue,  BlockingQueue<BcpData> cleanUpQueue,int numberOfThread, DbUtils dbUtils, 
			boolean insertToHeader, boolean insertToFact, int retryThreshold, GlobalStatusUtil globalStatus) {
		
		this.doneQueue = doneQueue;
		this.cleanUpQueue = cleanUpQueue;
		this.numberOfThread = numberOfThread;
		this.doneExecutor =  Executors.newFixedThreadPool( numberOfThread );
		this.dbUtils = dbUtils;
		this.insertToHeader = insertToHeader;
		this.insertToFact   = insertToFact;
		this.retryThreshold = retryThreshold;
		this.globalStatus =  globalStatus;
		
	}
    
   
	@Override
	public void run() {
		
	
		BcpData bcpData;
		try {
			while ((bcpData = doneQueue.take()) != null) {
				
				try {
					   logger.info("picked up in donequeue:" + bcpData.getName() + ":" + bcpData.getId());
						   doneExecutor.submit( new BcpDoneTask(bcpData) );
					
					   
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("Exiting BcpDone Thread Not Good");

	}
	
	public void setNumberOfThread(int numberOfThread) {
		this.numberOfThread = numberOfThread;
	}


	public int getNumberOfThread() {
		return numberOfThread;
	}

	public void setDbUtils(DbUtils dbUtils) {
		this.dbUtils = dbUtils;
	}


	public DbUtils getDbUtils() {
		return dbUtils;
	}

	class BcpDoneTask implements Callable<Long>{
		
		BcpData bcpData;
		
		public BcpDoneTask(BcpData bcpData) {
			this.bcpData = bcpData;
		}

	

		@Override
		public Long call() throws Exception {
			long startTime = System.currentTimeMillis();
			 BcpUtil bcpUtils = new BcpUtil( cleanUpQueue,doneQueue,insertToHeader,insertToFact,retryThreshold ,dbUtils, globalStatus);
	         bcpUtils.process(bcpData);
	         logger.info("Done thread Exiting" );
	         return System.currentTimeMillis() - startTime;
	         
		}
		
		
	}
}
