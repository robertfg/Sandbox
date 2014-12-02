package com.anz.rer.etl.csvToTable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.anz.rer.etl.utils.DbUtils;
import com.anz.rer.etl.utils.GlobalStatusUtil;

public class BcpExecCommandPool implements Runnable{
   
	private final static Logger logger = Logger.getLogger(BcpExecCommandPool.class);
	private BlockingQueue<BcpData> cmdQueue;
	private BlockingQueue<BcpData> doneQueue;
	private ExecutorService bcpExecCmdPool;
	
	private int numberOfThread;
	
	private DbUtils dbUtils; 
	private BlockingQueue<BcpData> cleanUpQueue;
	
	private Map<String,BcpConfig> bcpConfig = new HashMap<String,BcpConfig>();
	
	private GlobalStatusUtil globalStatus;

	public BcpExecCommandPool(BlockingQueue<BcpData> cmdQueue,
		 	 BlockingQueue<BcpData> doneQueue,
			 int numberOfThread,
			 Map<String,BcpConfig> bcpConfig
			,DbUtils dbUtils,GlobalStatusUtil globalStatus,BlockingQueue<BcpData> cleanUpQueue
	
			) {
		super();
		this.cmdQueue = cmdQueue;
		this.doneQueue = doneQueue;

		
		this.bcpConfig =  bcpConfig;
		bcpExecCmdPool =  Executors.newFixedThreadPool( numberOfThread);
		this.dbUtils =  dbUtils;
		this.globalStatus = globalStatus;
		this.cleanUpQueue = cleanUpQueue;
		
		
	}

	public void run() {
	 	BcpData bcpData;
	 	
		try {
			while ((bcpData = cmdQueue.take()) != null) {
				logger.debug("getting from sqlQueue:" + bcpData.getName() + " :" + bcpData.getId() + " Done:" + bcpData.isDone());
				try {
					bcpExecCmdPool.execute( new BcpExecCommand(bcpData,  this.getBcpConfig(bcpData.getName()), doneQueue,dbUtils, globalStatus,cleanUpQueue ) );
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
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
	

	
	