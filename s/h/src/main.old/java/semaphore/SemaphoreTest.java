package semaphore;

import java.util.concurrent.Semaphore;

//Read more: http://javarevisited.blogspot.com/2012/05/counting-semaphore-example-in-java-5.html#ixzz383wxtSqL

public class SemaphoreTest {

    Semaphore binary = new Semaphore(1);
  
    public static void main(String args[]) {
        final SemaphoreTest test = new SemaphoreTest();
        new Thread(){
            @Override
            public void run(){
              test.mutualExclusion(); 
            }
        }.start();
      
        new Thread(){
            @Override
            public void run(){
              test.mutualExclusion(); 
            }
        }.start();
      
    }
  
    private void mutualExclusion() {
        try {
            binary.acquire();
  
            //mutual exclusive region
            System.out.println(Thread.currentThread().getName() + " inside mutual exclusive region");
            Thread.sleep(10000);

        } catch (InterruptedException i) {
            i.printStackTrace();
        	
        } finally {
            binary.release();
            System.out.println(Thread.currentThread().getName() + " outside of mutual exclusive region");
        }
    } 
  
}

