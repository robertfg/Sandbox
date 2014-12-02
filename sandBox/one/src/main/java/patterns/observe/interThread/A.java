package patterns.observe.interThread;

import java.util.Observable;
import java.util.Observer;

class A  implements Observer {

	private boolean runIt = true;
    public void update(Observable o, final Object arg) {
    	
    	new Thread(){
    		
    		  @Override
      		public void run() {
      			
      	    	System.out.println("hi" + arg);
      	        try {
      				System.out.println("i'm going to sleep put block");
      		    	Thread.sleep(5000);
      		    	System.out.println("i'm awake now");
      		    	
      			} catch (InterruptedException e) {
      				
      				e.printStackTrace();
      			}
      		}    
    	}.start();
          
    }
    
      

	    

}