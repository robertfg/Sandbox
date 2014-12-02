package produceConsume.join;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Join {
	  
    public static void main(String args[]) throws InterruptedException{
      
        System.out.println(Thread.currentThread().getName() + " is Started");
      
        Thread exampleThread = new Thread(){
            public void run(){
                try {
                    System.out.println(Thread.currentThread().getName() + " is Started");
                    Thread.sleep(2000);
                    System.out.println(Thread.currentThread().getName() + " is Completed");
                } catch (InterruptedException ex) {
                    Logger.getLogger(Join.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
      
        exampleThread.start();
        exampleThread.join(); // making current thread wait and proceed once the exampleThread finish
      
        System.out.println(Thread.currentThread().getName() + " is Completed");
    }
  
}

/*
If you look at above example, first main thread is started and than it creates another thread, whose name is "Thread-0" and started it. Since Thread-0 sleep for 2 seconds, it require at least 2 seconds to complete and in between main thread called join method on Thread-0 object. Because of join method, now main thread will wait until Thread-0 completes its operation or You can say main thread will join Thread-0. If you look on output, it confirms this theory.

Important point on Thread.join method
Now we know How to use join method in Java, it’s time to see some important points about Thread.join() method.

1. Join is a final method in java.lang.Thread class and you cannot override it.
2) Join method throw IntrupptedException if another thread interrupted waiting thread as a result of join() call.

3) Join is also an overloaded method in Java, three version of join() available, check javadoc for details.


Read more: http://javarevisited.blogspot.com/2013/02/how-to-join-multiple-threads-in-java-example-tutorial.html#ixzz3DXWcRMKV
 * */


//Read more: http://javarevisited.blogspot.com/2013/02/how-to-join-multiple-threads-in-java-example-tutorial.html#ixzz3DXVc23Zg
	