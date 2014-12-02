package blockingqueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

 
public class Server extends Thread {
   //private BlockingQueue<Request> queue = new LinkedBlockingQueue<Request>();
   static final int CAPACITY = 2;

   private BlockingQueue<Request> queue = new LinkedBlockingQueue<Request>(CAPACITY);
 /*
   public void accept(Request request) {
      try {
         queue.put(request); //BlockingQueue method put (see Listing 5).
                             // Calls to this method will block until 
                             // the queue has available capacity.
      }
      catch (InterruptedException e) {
         throw new RuntimeException("add to queue interrupted");
      }
   }*/
   
  public void accept(Request request) {
      queue.add(request); // But, once the queue contains two elements, 
                          // attempts to add additional elements immediately throw an IllegalStateException. 
   }
   /*
   public boolean accept(Request request) {
	      return queue.offer(request); //  method, also defined in the BlockingQueue interface. 
	                                   // This method returns false immediately when the queue is already full.
	   }
   */
   public void run() {
      while (true)
         try {
            execute(queue.take());
         }
         catch (InterruptedException e) {
         }
   }

   private void execute(final Request request) {
      new Thread(new Runnable() {
         public void run() {
            request.execute();
         }
      }).start();
   }
}