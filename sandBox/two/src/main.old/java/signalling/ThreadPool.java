package signalling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool implements Runnable{

   
	private ExecutorService executorService;
	private SingletonSignalThread signal;
	MonitorObject myMonitorObject = new MonitorObject();
	Thread me;
	
	public ThreadPool(SingletonSignalThread signal) {
		
		me = new Thread(this);
		me.start();
		
		executorService = Executors.newFixedThreadPool(10,new ThreadFactory() {
			AtomicInteger threadCtr = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				 Thread t = new Thread(r,"ExtractionService-" + threadCtr.incrementAndGet());
				 return t;
			}
		});
		
		this.signal = signal;
		
	}

	@Override
	public void run() {
		
		// synchronized(signal){
		synchronized (this) {
			while (true) {
				while (suspendFlag) {
					try {
						System.out.println("Giving in with other request");
						wait();
						System.out.println("I will continue now what I was doing");

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("processing.....");

			}
		}

		// clear signal and continue running.
		// wasSignalled = false;
		// }
		
	}
	boolean suspendFlag;
	
	public void mysuspend(){
		suspendFlag = true;
	}

	 public synchronized void myresume() {
		 suspendFlag = false;
		 notify();
	 }
}
