package locks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockingUtil  {
	
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	private ITediousTask tediousTask;
	 
	public LockingUtil(ITediousTask tediousTask){
		this.tediousTask = 	tediousTask;
	}
	
	
	
	public void execute() {
		lock.writeLock().lock();
		
		try {
			doTediousTask();
		} catch (Exception e) {

		} finally {
			lock.writeLock().unlock();
		}

	}

	/*public void readDataFromCache() {
		lock.readLock().lock();
		try {
		} catch (Exception e) {

		} finally {
			lock.readLock().unlock();
		}

	}*/

	private void doTediousTask(){
		
		ExecutorService  executorService = Executors.newFixedThreadPool(1,new ThreadFactory() {
		    AtomicInteger threadCtr = new AtomicInteger();
		    @Override
			public Thread newThread(Runnable r) {
				 Thread t = new Thread(r,"TediousTaskDoer-" + threadCtr.incrementAndGet());
				 return t;
			}
		});
		
		executorService.submit(tediousTask);
	}
}
