package com.anz.rer.etl.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.anz.rer.etl.transform.impl.TransposeConfig;
import com.anz.rer.etl.transform.impl.TransposeCsv;
import com.anz.rer.etl.utils.CsvObject;

public class TransposeTask implements Callable<Long> {

	private BlockingQueue<CsvObject> sourceQueue;
	private BlockingQueue<CsvObject> destQueue;
	private TransposeConfig   transposeConfig;
	//private ExecutorService transposeService; 

	private final static Logger logger = Logger.getLogger(TransposeTask.class);

	 public TransposeTask(BlockingQueue<CsvObject> sourceQueue, BlockingQueue<CsvObject> destQueue,	 TransposeConfig transposeConfig ) {
		this.sourceQueue = sourceQueue;
		this.destQueue = destQueue;
		this.transposeConfig = transposeConfig;
	   
	/*	transposeService = Executors.newFixedThreadPool(12,new ThreadFactory() {
			    AtomicInteger threadCtr = new AtomicInteger();
			    @Override
				public Thread newThread(Runnable r) {
					 Thread t = new Thread(r,"Transposer-" + threadCtr.incrementAndGet());
					 return t;
				}
			});*/
	 
	 }
	 
	@Override
	public Long call() throws Exception {
		long totalTime = 0;
		CsvObject csvObject;
        int ctr = 0;
		while ((csvObject = sourceQueue.take()) != null) {
			logger.debug("TransposeTask Take..........." );
			
				  if (csvObject.isDone()) {
			        logger.debug("Shutting Down TransposeTask...........");
			        this.destQueue.put(csvObject);
			        break;
			      }
			      logger.info("transposeTask put:" + ctr);
			      totalTime += call(csvObject).longValue();
			      ctr++;
			/*
			 * to enable transaction coordinator
			 * logger.info("transposeTask put:" + ctr);
			totalTime+=call(csvObject);			
			if (csvObject.isDone()){
				logger.debug("Shutting Down TransposeTask..........." );
				destQueue.put(csvObject);
				break;
			}
			ctr++;*/
		}
		logger.info("Total time to process:" +  csvObject.getName() + " " + totalTime + "ms" );
		//transposeService.shutdown();
		return totalTime;
	}

	private Long call(CsvObject csvObject) throws InterruptedException{
		long start = System.currentTimeMillis();
		//transposeService.submit( new TransposeCsv( transposeConfig,destQueue,csvObject));
		
		/*if( csvObject.getName().contains("VAR_STRESS") || csvObject.getName().contains("PNL-VAR_") || csvObject.getName().contains("IR_GAMMA")){ 
			
			transposeService.submit( new TransposeCsv( transposeConfig,destQueue,csvObject));
		
		}else {*/
			
		
			 TransposeCsv transposeCsv = new TransposeCsv( transposeConfig);
			 CsvObject csvObj = transposeCsv.transform(csvObject);
			 
			
			if(csvObj!=null){
				logger.info("TransposeTask putting to stamping");
				destQueue.put(csvObj);
			}else{
				logger.info("TransposeTask csvObject is null");
			}  
		//}
		return System.currentTimeMillis() - start;
	}
	
}
