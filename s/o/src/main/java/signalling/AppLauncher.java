package signalling;

public class AppLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SingletonSignalThread signal = new SingletonSignalThread();
		
		ThreadPool  threadPool = new ThreadPool(signal);
		 
        
		try {
			Thread.sleep(10000);
			threadPool.mysuspend();
			
			signal.setWait(true);
			Thread.sleep(10000);
			threadPool.myresume();
			signal.setWait(false);
			
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		
	}

	
}

