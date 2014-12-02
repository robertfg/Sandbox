package produceConsume.interThread;

import java.util.Queue;
import java.util.logging.Logger;

public class Producer extends Thread {
    private static final Logger logger = Logger.getLogger(Producer.class.getName());
    
    private final Queue sharedQ;

    public Producer(Queue sharedQ) {
        super("Producer");
        this.sharedQ = sharedQ;
    }

    @Override
    public void run() {

        for (int i = 0; i < 4; i++) {

            synchronized (sharedQ) {
                //waiting condition - wait until Queue is not empty
                while (sharedQ.size() >= 1) {
                    try {
                        logger.info("Queue is full, waiting");
                        sharedQ.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                logger.info("producing : " + i);
                sharedQ.add(i);
                sharedQ.notify();
            }
        }
    }
}