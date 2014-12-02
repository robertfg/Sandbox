package asynch;

public class AsyncExecutor {

	public static void main(String[] args) {
      
	
		

		AsyncCallback<String> callBack = new AsyncCallback<String>() {
				
				String res ="";
				private int count;
		
				public void callback(String result) throws Exception {
					res = res + result;
					System.out.println(">>>>>>>>>>>>>>>>>>" + res);
				}
				
				public void setTaskCount(int task) {
					count = task;
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
					return "X-" ;
				}
				}).start();
			
		}
		

		
		System.out.println("--DONE ! --");
	}

}