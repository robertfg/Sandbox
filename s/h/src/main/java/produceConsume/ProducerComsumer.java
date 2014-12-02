package produceConsume;

public class ProducerComsumer extends Object {
	  private Object slot;

	  public ProducerComsumer() {
	    slot = null; // null indicates empty
	  }

	  public synchronized void putIn(Object obj) 
	            throws InterruptedException {

	    while ( slot != null ) {
	      wait(); 
	    }

	    slot = obj;  // put object into slot
	    notifyAll(); // signal that slot has been filled
	  }

	  public synchronized Object takeOut() 
	            throws InterruptedException {

	    while ( slot == null ) {
	      wait(); // wait while slot is empty
	    }

	    Object obj = slot;
	    slot = null; // mark slot as empty
	    notifyAll(); // signal that slot is empty
	    return obj;
	  }
	  public static void main(String[] args) {
	    final ProducerComsumer ch = new ProducerComsumer();

	    Runnable runA = new Runnable() {
	        public void run() {
	          try {
	            String str;
	            Thread.sleep(10000);

	            str = "multithreaded";
	            ch.putIn(str);
	            str = "programming";
	            ch.putIn(str);

	            str = "with Java";
	            ch.putIn(str);
	          } catch ( InterruptedException x ) {
	            x.printStackTrace();
	          }
	        }
	      };

	    Runnable runB = new Runnable() {
	        public void run() {
	          try {
	            Object obj;

	            obj = ch.takeOut();
	            System.out.println("in run() - just took out: '" + 
	                obj + "'");

	            Thread.sleep(5000);

	            obj = ch.takeOut();
	            System.out.println("in run() - just took out: '" + 
	                obj + "'");

	            obj = ch.takeOut();
	            System.out.println("in run() - just took out: '" + 
	                obj + "'");
	          } catch ( InterruptedException x ) {
	            x.printStackTrace();
	          }
	        }
	      };

	      Thread threadB = new Thread(runB, "threadB");
		    threadB.start();
	      Thread threadA = new Thread(runA, "threadA");
	      threadA.start();

	    
	  }
	  
	}
