package locks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TediousTaskUtil {
	
	private ITediousTask<Boolean> tediousTask;
	private AtomicBoolean started = new AtomicBoolean(false);
	
	public TediousTaskUtil(ITediousTask<Boolean> tediousTask) {
		this.tediousTask = tediousTask;
	}
	
	public void execute(){
		
		if( started.get() == false ){
			started.set(true);
			   
			ExecutorService  executorService = Executors.newFixedThreadPool(1,new ThreadFactory() {
			    AtomicInteger threadCtr = new AtomicInteger();
			    @Override
				public Thread newThread(Runnable r) {
					 Thread t = new Thread(r,"TediousTaskDoer-" + threadCtr.incrementAndGet());
					 return t;
				}
			});
			
			Future<Boolean> task = executorService.submit(tediousTask);
					//	task.get();
			
			
		}
		
		
	}
	
	
	
	
}
