package com.anz.rer.etl.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.anz.rer.etl.utils.CsvObject;
import com.anz.rer.etl.vectorizer.VectorConfig;
import com.anz.rer.etl.vectorizer.Vectorizer;

public class VectorTask implements Callable<Long> {

	private final static Logger logger = Logger.getLogger(VectorTask.class);
    
	private BlockingQueue<CsvObject> sourceQueue;
	private BlockingQueue<CsvObject> destQueue;
	private VectorConfig   vectorConfig;

	 public VectorTask(BlockingQueue<CsvObject> sourceQueue,
			 BlockingQueue<CsvObject> destQueue,
			VectorConfig vectorConfig ) {
		this.sourceQueue = sourceQueue;
		this.destQueue = destQueue;
		this.vectorConfig = vectorConfig;
	}
	 
	@Override
	public Long call() throws Exception {
		long totalTime = 0;
		CsvObject csvObject;

		while ((csvObject = sourceQueue.take()) != null) {
			logger.debug("Vector take" );
			if (csvObject.isDone()){
				logger.debug("Shutting Down Vector..........." );
				destQueue.put(csvObject);
				logger.debug("exiting while Vector..........." );
				break;
			}
			totalTime+=call(csvObject);			
		}
		logger.info("Total time to process:" +  csvObject.getName() + " " + totalTime + "ms" );
		
		return totalTime;
	}
	
	private Long call(CsvObject csvObject) {
		long start = System.currentTimeMillis();
		Vectorizer vectorizer = new Vectorizer();
		csvObject =   vectorizer.vectorizer(csvObject, vectorConfig);
		
		if(csvObject!=null){
			try {
				destQueue.put(csvObject);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		} else {
			logger.debug("..................................................Vector NULLL to next........." );
			
			
		}    
		return System.currentTimeMillis() - start;
		
	}
	
	
	public static void main(String[] args){
		
		String file =  "TAZC0#ANZ_STRESS_CREDIT#NON-VAR#2465009195910330#20130214#2465009195910330.APX";
		String[] ignoreFileList = new String[]{"VAR_STRESS" , "HYPO"};
		
		for (String fileName : ignoreFileList) { 
			if(file.indexOf(fileName)!=-1){
				System.out.println(" test ");
			}else {
				
				System.out.println("not");
				
			}
		}
		
		
	}
	
	
	
}
