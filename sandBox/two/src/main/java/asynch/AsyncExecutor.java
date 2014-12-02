package asynch;

import java.util.concurrent.atomic.AtomicInteger;

public class AsyncExecutor {

	public static void main(String[] args) {
      
	
		final int taskX = 3;
        final AtomicInteger counter = new AtomicInteger(0);
        
		AsyncCallback<String> callBack = new AsyncCallback<String>() {
				
				String res ="";
				private int count;
		
				public void callback(String result) throws Exception {
					res = res + result;
					counter.getAndIncrement();
					
					System.out.println( counter.get() );
					//if(taskX == counter.get()){
						System.out.println(counter.get()  + ",Result:" + res);	
					//}
					
				} 
				
				public void setTaskCount(int task) {
					count = count + task;
					//System.out.println("count:" + count);
				}
				
				
				
		};
			
		
		AsyncErrback errCallBack =	new AsyncErrback() {
			public void errback(Exception error) {
				System.out.println("Oop's something wrong happened - > " + error.getMessage());
			}
		};
		
		for (int i = 0; i < 3; i++) {
		
			final String x = i + " ";
			
			new Thread(new AsyncTask<String>( callBack, errCallBack) {
				@Override
				public String executeTask() throws Exception {
					System.out.println("Doing Something complicated -> don't wait for me to complete.");
					return "X" ;
					
				}
				}).start();
			
		}
		

		
		System.out.println("--DONE ! --");
	}

}