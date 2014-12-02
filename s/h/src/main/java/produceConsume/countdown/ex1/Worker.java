package produceConsume.countdown.ex1;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Worker implements Runnable {
	 
    private CountDownLatch countDownLatch;
 
    public Worker(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
 
    @Override
    public void run() {
        try {
            Thread.sleep(getRandomSeconds()); // sleep random time to simulate long running task
            System.out.println("Counting down: " + Thread.currentThread().getName());
            this.countDownLatch.countDown();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
 
    // returns a long between 0 and 9999
    private long getRandomSeconds() {
        Random generator = new Random();
        return Math.abs(generator.nextLong() % 10000);
    }
} 