package com.anz.rer.etl.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.anz.rer.etl.cache.LookUp;
import com.anz.rer.etl.utils.CsvObject;
import com.anz.rer.etl.worker.StampCsv;

public class StampTask implements Callable<Long>{

	private final static Logger logger = Logger.getLogger(StampTask.class);
	private BlockingQueue<CsvObject> sourceQueue;
	private BlockingQueue<CsvObject> destQueue;
	private LookUp cache;
	private boolean lock = false;
	
	public StampTask(BlockingQueue<CsvObject> sourceQueue,
			BlockingQueue<CsvObject> destQueue, LookUp cache) {
	
		this.sourceQueue = sourceQueue;
		this.destQueue = destQueue;
		this.cache = cache;
	}


	
	
	
	@Override
	public Long call() throws Exception {
		long totalTime = 0;
		CsvObject csvObject;

		while ((csvObject = sourceQueue.take()) != null) {
			logger.debug("StampTask taking ............................" );
			
			if (csvObject.isDone()){
				logger.debug("Shutting Down StampTask" );
				destQueue.put(csvObject);
				break;
			}
			totalTime+=call(csvObject,cache);	
					
		}
		logger.info("Total time to process:" +  csvObject.getName() + " " + totalTime + "ms" );
		
		return totalTime;
	}

	private Long call(CsvObject csvObject,LookUp cache) {
		logger.info("StampTask Cache ID:" + cache.hashCode()); 
		long start = System.currentTimeMillis();
		StampCsv stampCsv = new StampCsv(cache);
		CsvObject csvObj = stampCsv.stampCsv(csvObject);
			
		if(csvObj!=null){
			try {
				destQueue.put(csvObj);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}  
		return System.currentTimeMillis() - start;
	}
	
	

} 
