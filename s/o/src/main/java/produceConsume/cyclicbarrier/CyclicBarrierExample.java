package produceConsume.cyclicbarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CyclicBarrierExample {

    //Runnable task for each thread
    private static class Task implements Runnable {

        private CyclicBarrier barrier;

        public Task(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " is waiting on barrier");
                barrier.await();
                System.out.println(Thread.currentThread().getName() + " has crossed the barrier");
            } catch (InterruptedException ex) {
                Logger.getLogger(CyclicBarrierExample.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BrokenBarrierException ex) {
                Logger.getLogger(CyclicBarrierExample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String args[]) {

        //creating CyclicBarrier with 3 parties i.e. 3 Threads needs to call await()
        final CyclicBarrier cb = new CyclicBarrier(3, new Runnable(){
            @Override
            public void run(){
                //This task will be executed once all thread reaches barrier
                System.out.println("All parties are arrived at barrier, lets play");
            }
        });

        //starting each of thread
        Thread t1 = new Thread(new Task(cb), "Thread 1");
        Thread t2 = new Thread(new Task(cb), "Thread 2");
        Thread t3 = new Thread(new Task(cb), "Thread 3");

        t1.start();
        t2.start();
        t3.start();
      
    }
}


//Read more: http://javarevisited.blogspot.com/2012/07/cyclicbarrier-example-java-5-concurrency-tutorial.html#ixzz3DYAuT6fo
/*
When to use CyclicBarrier in Java
Given the nature of CyclicBarrier it can be very handy to implement map reduce kind of task similar to fork-join framework of Java 7,
 where a big task is broker down into smaller pieces and to complete the task you need output from individual small task e.g.
  to count population of India you can have 4 threads which counts population from North, South, East and West 
  and once complete they can wait for each other, 
  When last thread completed there task, Main thread or any other thread can add result from each zone and print total population. 
  You can use CyclicBarrier in Java :

1) To implement multi player game which can not begin until all player has joined.
2) Perform lengthy calculation by breaking it into smaller individual tasks, In general to implement Map reduce technique.

Important point of CyclicBarrier in Java
1. CyclicBarrier can perform a completion task once all thread reaches to barrier, This can be provided while creating CyclicBarrier.

2. If CyclicBarrier is initialized with 3 parties means 3 thread needs to call await method to break the barrier.
3. Thread will block on await() until all parties reaches to barrier, another thread interrupt or await timed out.
4. If another thread interrupt the thread which is waiting on barrier it will throw BrokernBarrierException as shown below:

java.util.concurrent.BrokenBarrierException
        at java.util.concurrent.CyclicBarrier.dowait(CyclicBarrier.java:172)
        at java.util.concurrent.CyclicBarrier.await(CyclicBarrier.java:327)

5.CyclicBarrier.reset() put Barrier on its initial state, other thread which is waiting or not yet reached barrier will terminate with java.util.concurrent.BrokenBarrierException.

That's all on  What is CyclicBarrier in Java , When to use CyclicBarrier in Java and a Simple Example of How to use CyclicBarrier in Java . We have also seen difference between CountDownLatch and CyclicBarrier in Java and got some idea where we can use CyclicBarrier in Java Concurrent code.


Read more: http://javarevisited.blogspot.com/2012/07/cyclicbarrier-example-java-5-concurrency-tutorial.html#ixzz3DYCrRA2j
*/

