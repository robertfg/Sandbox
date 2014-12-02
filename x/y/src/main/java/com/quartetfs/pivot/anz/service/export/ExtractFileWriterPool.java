package com.quartetfs.pivot.anz.service.export;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.quartetfs.pivot.anz.impl.MessagesANZ;

public class ExtractFileWriterPool implements Runnable {
	
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private ExecutorService fileWriterExecutor;   
	private BlockingQueue<ExtractObject> fileWriterQueue;
	private int fileWritePartition;
	
	private ConcurrentMap<Long,ExtractFileWriter> threadMgr = new ConcurrentHashMap<Long,ExtractFileWriter>();
	
	private ExtractCoordinator extractCoordinator;
	
	private Thread me;
     
	
	 
	 public ExtractFileWriterPool(int fileWriterQueueSize, int fileWriterExecutorSize, int fileWritePartition,ExtractCoordinator extractCoordinator ){
		 fileWriterQueue = new ArrayBlockingQueue<ExtractObject>(fileWriterQueueSize,true);
		  
		 fileWriterExecutorSize=fileWriterExecutorSize==-1?Runtime.getRuntime().availableProcessors():fileWriterExecutorSize;
		    
		 fileWriterExecutor = Executors.newFixedThreadPool(fileWriterExecutorSize, new ThreadFactory() {
				AtomicInteger threadCtr = new AtomicInteger();
				@Override
				public Thread newThread(Runnable r) {
					 Thread t = new Thread(r, "FileWriterThread" + ":" + threadCtr.incrementAndGet());
					 return t;
				}
			});
		  
		 this.extractCoordinator = extractCoordinator;
		  
		  this.fileWritePartition = fileWritePartition;
		  me = new Thread(this,"FileWriterPool");
		  me.start();
	   }
	
	
	@Override
	public void run() {
		ExtractObject extractObject;
		ExtractFileWriter extractFileWriterThread = null;
		
		try {
			while ((extractObject = fileWriterQueue.take()) != null) {
				
				try {
					
					LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>GETTING>>>>>>>>>>>>>>>FileWriterPool:" + extractObject.getId());
					extractFileWriterThread = threadMgr.get(extractObject.getId());
					
					if(null!=extractFileWriterThread){
						LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>EXISTING>>>>>>>>>>>>>>>FileWriterPool:" + extractObject.getId());
						extractFileWriterThread.getFileDataQueue().put(extractObject);
						
						if( extractCoordinator.transactionDone(extractObject)) { // extractObject.isDone()) { // and count
							LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>> REMOVING >>>>>>>>>>>>>>>FileWriterPool:" + extractObject.getId());
							threadMgr.remove(extractObject.getId());
							extractFileWriterThread = null;
						}
						
						
					} else {
						  LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>CREATING>>>>>>>>>>>>>>>FileWriterPool:" + extractObject.getId());
						 
						  extractFileWriterThread =  new ExtractFileWriter( extractObject,fileWritePartition, 5);
						  threadMgr.put(extractObject.getId(), extractFileWriterThread);
						  fileWriterExecutor.submit( extractFileWriterThread ); 
					}
					 
					extractObject = null;
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	public BlockingQueue<ExtractObject> getFileWriterQueue() {
		return fileWriterQueue;
	}

	public void setFileWriterQueue(BlockingQueue<ExtractObject> fileWriterQueue) {
		this.fileWriterQueue = fileWriterQueue;
	}

}
