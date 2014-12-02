package produceConsume.countdown.ex1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//http://markusjais.com/how-to-use-java-util-concurrent-countdownlatch/
public class WorkManager {
	 
    private CountDownLatch countDownLatch;
    private static final int NUMBER_OF_TASKS = 5;
 
    public WorkManager() {
        countDownLatch = new CountDownLatch(NUMBER_OF_TASKS);
    }
 
    public void finishWork() {
        try {
            System.out.println("START WAITING");
            countDownLatch.await();
            System.out.println("DONE WAITING");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
 
    public void startWork() {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_TASKS);
 
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            Worker worker = new Worker(countDownLatch);
            executorService.execute(worker);
        }
        executorService.shutdown();
    }
 
    public static void main(String[] args) {
        WorkManager workManager = new WorkManager();
        System.out.println("START WORK");
        workManager.startWork();
        System.out.println("WORK STARTED");
        workManager.finishWork();
        System.out.println("FINISHED WORK");
    }
}