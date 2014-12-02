package com.quartetfs.pivot.anz.service.export;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.quartetfs.pivot.anz.impl.MessagesANZ;

public class ExtractAPQueryPool implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);

	private ExecutorService queryExecutor; 
	private BlockingQueue<Callable<ExportTaskThreadInfo>> queryQueue;
	
/*	private BlockingQueue<Runnable> queryQueue;
*/	
	//private List<Future<ExportTaskThreadInfo>> results = new ArrayList<Future<ExportTaskThreadInfo>>();
	
	private Thread me;

	 public ExtractAPQueryPool(int queryQueueSize, int queryExecutoSize ){
		 
		 //queryQueue = new ArrayBlockingQueue<Callable<ExportTaskThreadInfo>>(queryQueueSize, true);
		
		 
		 queryQueue = new LinkedBlockingQueue<Callable<ExportTaskThreadInfo>>(queryQueueSize);
		 
		 //queryQueue = new LinkedBlockingQueue<Runnable>(queryQueueSize);
			
		 
		 queryExecutoSize=queryExecutoSize==-1?Runtime.getRuntime().availableProcessors():queryExecutoSize;
		 
		 
		 queryExecutor = Executors.newFixedThreadPool(queryExecutoSize, new ThreadFactory() {
				AtomicInteger threadCtr = new AtomicInteger();
				@Override
				public Thread newThread(Runnable r) {
					 Thread t = new Thread(r, "ExtractAPQueryThread" + ":" + threadCtr.incrementAndGet());
					 return t;
				}
			});
		 
		  me =  new Thread(this,"ExtractAPQueryPool");
		  me.start();
	   }
	
	@Override
	public void run() {
		Callable<ExportTaskThreadInfo> apQueryObject;
		//Runnable apQueryObject;
		
		try {
			while ((apQueryObject = queryQueue.take()) != null) {
				try {
					
					//new Thread(apQueryObject).start();
					Future<ExportTaskThreadInfo> result = queryExecutor.submit(apQueryObject);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	public BlockingQueue<Callable<ExportTaskThreadInfo>> getQueryQueue() {
		return queryQueue;
	}

	public void setQueryQueue(BlockingQueue<Callable<ExportTaskThreadInfo>> queryQueue) {
		this.queryQueue = queryQueue;
	}

    public void printAllFinishThread(){
    }

}
