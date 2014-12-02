package asynch;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CalledBack implements CallbackInterface{
    Object result;
    
    public CalledBack() {
    }  

    public void returnResult(Object result) {
      System.out.println("vvvvvvvvvvvvvvvvvResult Received "+result);
      this.result = result;
    }
    
    public void andAction() throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(3);
        CallingBackWorker worker = new CallingBackWorker();
        worker.setEmployer(this);
        final Future future = es.submit( worker);
        System.out.println("... try to do something while the work is being done....");
        System.out.println("... and more ....");
//        try {
//            future.get();
//        } catch (InterruptedException e) {
//        } catch (ExecutionException e) {
//        }
      //  System.out.println("Result is "+result);
        System.out.println("XXX End work:" + new java.util.Date());
      //  System.exit(0);

        Thread.sleep(20000);
        
    }  
    
    public static void main(String[] args) {
        try {
			new CalledBack().andAction();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
