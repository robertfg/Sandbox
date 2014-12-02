package com.anz.rer.etl.vectorizer;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.anz.rer.etl.cache.LookUp;
import com.anz.rer.etl.config.CsvReaderTaskConfig;
import com.anz.rer.etl.task.CsvReaderTask;
import com.anz.rer.etl.task.StampTask;
import com.anz.rer.etl.task.TransposeTask;
import com.anz.rer.etl.task.VectorTask;
import com.anz.rer.etl.transform.impl.TransposeConfig;
import com.anz.rer.etl.utils.CsvObject;
import com.anz.rer.etl.utils.GzipWriterTask;

public class VectorizerTask implements Callable<Long> {

	private final static Logger logger = Logger.getLogger(VectorizerTask.class);
    
	private File sourceFile;
    private VectorConfig vectorConfig;
    private CsvReaderTaskConfig csvReaderTaskConfig;
    private TransposeConfig transposeConfig;
    private int queueSize = 10;
    private LookUp cache;
    
    public VectorizerTask(File sourceFile, VectorConfig vectorConfig,
			CsvReaderTaskConfig csvReaderTaskConfig, TransposeConfig transposeConfig,
			int queueSize,LookUp cache) {
		
		this.sourceFile = sourceFile;
		this.vectorConfig = vectorConfig;
		this.csvReaderTaskConfig = csvReaderTaskConfig;
		this.transposeConfig = transposeConfig;
		this.queueSize = queueSize;
		this.cache = cache;
		logger.info("VectorizerTask Cache ID:" + cache.hashCode());
		
	}

    

	@Override
	public Long call() throws Exception {
		
		long ioTime =0;
		   ExecutorService vectorizerService = newExecutorService(5) ;
		   try{
		   String tempFileName = String.format("%s.gz.tmp", sourceFile.getName());
			
		  
		   BlockingQueue<CsvObject> csvRecordsQueue =  new ArrayBlockingQueue<CsvObject>(queueSize,true);
		   BlockingQueue<CsvObject> vectorizedQueue =  new ArrayBlockingQueue<CsvObject>(queueSize,true); 
		   BlockingQueue<CsvObject> transposeQueue  =  new ArrayBlockingQueue<CsvObject>(queueSize,true);
		   BlockingQueue<CsvObject> stampQueue      =  new ArrayBlockingQueue<CsvObject>(queueSize,true);
			 
		    
		    
		   	vectorizerService.submit(new CsvReaderTask( csvRecordsQueue, csvReaderTaskConfig, sourceFile));
		   	vectorizerService.submit(new VectorTask( csvRecordsQueue,vectorizedQueue, vectorConfig));
		   	vectorizerService.submit(new TransposeTask( vectorizedQueue,transposeQueue, transposeConfig));
		 	vectorizerService.submit(new StampTask( transposeQueue,stampQueue, cache));
		   	
		   	Future<Long> totalTimeFuture = vectorizerService.submit( new GzipWriterTask(stampQueue,  
			new File(sourceFile.getParent() +"\\" + tempFileName)));
				ioTime=totalTimeFuture.get();	
		   
		   }catch(Exception e){
			   e.printStackTrace();
		   }finally{
			  vectorizerService.shutdown();  
			   
		   }
		
		return ioTime;
	}

	private ExecutorService newExecutorService(int thread){
		
		return Executors.newFixedThreadPool( thread,new ThreadFactory() {
			AtomicInteger threadCtr = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				 Thread t = new Thread(r,"Vectorizer-" + threadCtr.incrementAndGet());
				 return t;
			}
		});
	}
}
