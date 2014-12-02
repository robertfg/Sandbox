package com.quartetfs.pivot.anz.service.export;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.quartetfs.pivot.anz.utils.VectorConfig;
import com.quartetfs.pivot.anz.utils.Vectorizer;

public class VectorizerPool implements Runnable {

   private ExecutorService vectorizerExecutor; 
   private BlockingQueue<ExtractObject> vectorizerQueue;
   private BlockingQueue<ExtractObject> destQueue;
   
  
private Map<String, VectorConfig> vectorConfigurations = new HashMap<String, VectorConfig>();
	
   private Thread me;
   
   public VectorizerPool(int vectorizerQueueSize, int vectorizerExecutorSize){
	   vectorizerQueue = new ArrayBlockingQueue<ExtractObject>(vectorizerQueueSize,true);
	   
	   vectorizerExecutorSize=vectorizerExecutorSize==-1?Runtime.getRuntime().availableProcessors():vectorizerExecutorSize;
	   
	   
	   vectorizerExecutor = Executors.newFixedThreadPool(vectorizerExecutorSize,new ThreadFactory() {
			AtomicInteger threadCtr = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				 Thread t = new Thread(r, "Vectorizer" + ":" + threadCtr.incrementAndGet());
				 return t;
			}
		});
	   
	   me = new Thread(this,"VectorizerPool");
	   me.start();
   }

	@Override
	public void run() { 
		ExtractObject extractObject;
		try {
			while ((extractObject = vectorizerQueue.take()) != null) {
				
				try {
					
					vectorizerExecutor.submit(new Vectorizer(extractObject, vectorConfigurations.get( extractObject.getExtractType().toString() ) ,destQueue));
				    extractObject = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	   public BlockingQueue<ExtractObject> getDestQueue() {
			return destQueue;
		}

		public void setDestQueue(BlockingQueue<ExtractObject> destQueue) {
			this.destQueue = destQueue;
		}

		public BlockingQueue<ExtractObject> getVectorizerQueue() {
			return vectorizerQueue;
		}

		public void setVectorizerQueue(BlockingQueue<ExtractObject> vectorizerQueue) {
			this.vectorizerQueue = vectorizerQueue;
		}

		public Map<String, VectorConfig> getVectorConfigurations() {
			return vectorConfigurations;
		}

		public void setVectorConfigurations(
				Map<String, VectorConfig> vectorConfigurations) {
			this.vectorConfigurations = vectorConfigurations;
		}

	
	
}
