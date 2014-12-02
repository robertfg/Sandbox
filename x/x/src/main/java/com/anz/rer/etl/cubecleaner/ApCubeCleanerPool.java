package com.anz.rer.etl.cubecleaner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.anz.rer.etl.csvToTable.BcpData;


public class ApCubeCleanerPool implements Runnable{

	private Thread me;
	
	private BlockingQueue<BcpData> containerToRemove;
	private ExecutorService apCubeCleanerPoolExecutor;
	
	
	private final static Logger logger = Logger.getLogger(ApCubeCleanerPool.class);
	
	public ApCubeCleanerPool(BlockingQueue<BcpData> containerToRemove,int poolSize){
		
		apCubeCleanerPoolExecutor = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
				AtomicInteger threadCtr = new AtomicInteger();
				@Override
				public Thread newThread(Runnable r) {
					 Thread t = new Thread(r, "ApCubeCleaner:" + threadCtr.incrementAndGet());
					 return t;
				}
			});
		 
		  me =  new Thread(this,"ApCubeCleanerPool");
		  me.start();
		  
	}
	
	@Override
	public void run() {
		BcpData bcpData;
	 	
		try {
			while ((bcpData = containerToRemove.take()) != null) {
				logger.debug("removing container:" + bcpData.getName() + ",containerName:" + bcpData.getContainerName() + ",Id:" + bcpData.getId()  );
				try {
				
				//	apCubeCleanerPoolExecutor .s.execute( new BcpExecCommand(bcpData,  this.getBcpConfig(bcpData.getName()), doneQueue,dbUtils ) );
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
	}

	
	public static void main(String[] args){
		
		StringBuilder sb = new StringBuilder();
		for(int x = 1; x<=1540;x++){
			sb.append("S"+x+"$" + "S"+x+"$$" );
			
		}
		
		System.out.println( sb.toString() );
	}
	
	

}
