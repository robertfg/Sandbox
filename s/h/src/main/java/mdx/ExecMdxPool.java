package mdx;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class ExecMdxPool implements Runnable {

	
	private ExecutorService queryExecutor; 
	private BlockingQueue<Callable<Long>> queryQueue;

	private BlockingQueue<Callable<Long>> resultQueue;

	private Thread me;
	
	public ExecMdxPool(int queryQueueSize, int queryExecutoSize,BlockingQueue<Callable<Long>> queryQueue ){
		
		
	 	 queryQueue = new LinkedBlockingQueue<Callable<Long>>(queryQueueSize);
	 	 
		 queryExecutoSize=queryExecutoSize==-1?Runtime.getRuntime().availableProcessors():queryExecutoSize;
		 
		 
		 resultQueue = new LinkedBlockingQueue<Callable<Long>>(queryQueueSize);
	 	 
		 
		 queryExecutor = Executors.newFixedThreadPool(queryExecutoSize, new ThreadFactory() {
				AtomicInteger threadCtr = new AtomicInteger();
				@Override
				public Thread newThread(Runnable r) {
					 Thread t = new Thread(r, "ExecMdxPoolExecutor" + ":" + threadCtr.incrementAndGet());
					 return t;
				}
			});
		  me =  new Thread(this,"ExecMdxPool");
		  me.start();
		  
	}

	@Override
	public void run() {
		Callable<Long> apQueryObject;
		try {
			while ((apQueryObject = queryQueue.take()) != null) {
				try {
					Future<Long> result = queryExecutor.submit(apQueryObject);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}
	 

}
