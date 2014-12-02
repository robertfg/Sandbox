package threadpool;

public class ThreadPoolMain extends Object {

	  public static Runnable makeRunnable(final String name, final long firstDelay) {

	    return new Runnable() {
	      public void run() {
	        try {
	          System.out.println(name + ": starting up");
	          Thread.sleep(firstDelay);
	          System.out.println(name + ": doing some stuff");
	          Thread.sleep(2000);
	          System.out.println(name + ": leaving");
	        } catch (InterruptedException ix) {
	          System.out.println(name + ": got interrupted!");
	          return;
	        } catch (Exception x) {
	          x.printStackTrace();
	        }
	      }

	      public String toString() {
	        return name;
	      }
	    };
	  }

	  public static void main(String[] args) {
	    try {
	      ThreadPoolTwo pool = new ThreadPoolTwo(3);

	      Runnable ra = makeRunnable("RA", 3000);
	      pool.execute(ra);

	      Runnable rb = makeRunnable("RB", 1000);
	      pool.execute(rb);

	      Runnable rc = makeRunnable("RC", 2000);
	      pool.execute(rc);

	      Runnable rd = makeRunnable("RD", 60000);
	      pool.execute(rd);

	      Runnable re = makeRunnable("RE", 1000);
	      pool.execute(re);

	      pool.stopRequestIdleWorkers();
	      Thread.sleep(2000);
	      pool.stopRequestIdleWorkers();

	      Thread.sleep(5000);
	      pool.stopRequestAllWorkers();
	    } catch (InterruptedException ix) {
	      ix.printStackTrace();
	    }
	  }
	}

	class ThreadPoolTwo extends Object {
	  private ObjectFIFO idleWorkers;

	  private ThreadPoolWorker[] workerList;

	  public ThreadPoolTwo(int numberOfThreads) {
	    // make sure that it's at least one
	    numberOfThreads = Math.max(1, numberOfThreads);

	    idleWorkers = new ObjectFIFO(numberOfThreads);
	    workerList = new ThreadPoolWorker[numberOfThreads];

	    for (int i = 0; i < workerList.length; i++) {
	      workerList[i] = new ThreadPoolWorker(idleWorkers);
	    }
	  }

	  public void execute(Runnable target) throws InterruptedException {
	    // block (forever) until a worker is available
	    ThreadPoolWorker worker = (ThreadPoolWorker) idleWorkers.remove();
	    worker.process(target);
	  }

	  public void stopRequestIdleWorkers() {
	    try {
	      Object[] idle = idleWorkers.removeAll();
	      for (int i = 0; i < idle.length; i++) {
	        ((ThreadPoolWorker) idle[i]).stopRequest();
	      }
	    } catch (InterruptedException x) {
	      Thread.currentThread().interrupt(); // re-assert
	    }
	  }

	  public void stopRequestAllWorkers() {
	    stopRequestIdleWorkers();

	    try {
	      Thread.sleep(250);
	    } catch (InterruptedException x) {
	    }

	    for (int i = 0; i < workerList.length; i++) {
	      if (workerList[i].isAlive()) {
	        workerList[i].stopRequest();
	      }
	    }
	  }
	}

	class ThreadPoolWorker extends Object {
	  private static int nextWorkerID = 0;

	  private ObjectFIFO idleWorkers;

	  private int workerID;

	  private ObjectFIFO handoffBox;

	  private Thread internalThread;

	  private volatile boolean noStopRequested;

	  public ThreadPoolWorker(ObjectFIFO idleWorkers) {
	    this.idleWorkers = idleWorkers;

	    workerID = getNextWorkerID();
	    handoffBox = new ObjectFIFO(1); // only one slot

	    // just before returning, the thread should be created and started.
	    noStopRequested = true;

	    Runnable r = new Runnable() {
	      public void run() {
	        try {
	          runWork();
	        } catch (Exception x) {
	          // in case ANY exception slips through
	          x.printStackTrace();
	        }
	      }
	    };

	    internalThread = new Thread(r);
	    internalThread.start();
	  }

	  public static synchronized int getNextWorkerID() {
	    // notice: synchronized at the class level to ensure uniqueness
	    int id = nextWorkerID;
	    nextWorkerID++;
	    return id;
	  }

	  public void process(Runnable target) throws InterruptedException {
	    handoffBox.add(target);
	  }

	  private void runWork() {
	    while (noStopRequested) {
	      try {
	        System.out.println("workerID=" + workerID + ", ready for work");

	        idleWorkers.add(this);

	        Runnable r = (Runnable) handoffBox.remove();

	        System.out.println("workerID=" + workerID
	            + ", starting execution of new Runnable: " + r);
	        runIt(r);
	      } catch (InterruptedException x) {
	        Thread.currentThread().interrupt(); // re-assert
	      }
	    }
	  }

	  private void runIt(Runnable r) {
	    try {
	      r.run();
	    } catch (Exception runex) {
	      System.err.println("Uncaught exception fell through from run()");
	      runex.printStackTrace();
	    } finally {
	      Thread.interrupted();
	    }
	  }

	  public void stopRequest() {
	    System.out
	        .println("workerID=" + workerID + ", stopRequest() received.");
	    noStopRequested = false;
	    internalThread.interrupt();
	  }

	  public boolean isAlive() {
	    return internalThread.isAlive();
	  }
	}

	class ObjectFIFO extends Object {
	  private Object[] queue;

	  private int capacity;

	  private int size;

	  private int head;

	  private int tail;

	  public ObjectFIFO(int cap) {
	    capacity = (cap > 0) ? cap : 1; // at least 1
	    queue = new Object[capacity];
	    head = 0;
	    tail = 0;
	    size = 0;
	  }

	  public int getCapacity() {
	    return capacity;
	  }

	  public synchronized int getSize() {
	    return size;
	  }

	  public synchronized boolean isEmpty() {
	    return (size == 0);
	  }

	  public synchronized boolean isFull() {
	    return (size == capacity);
	  }

	  public synchronized void add(Object obj) throws InterruptedException {

	    waitWhileFull();

	    queue[head] = obj;
	    head = (head + 1) % capacity;
	    size++;

	    notifyAll();
	  }

	  public synchronized void addEach(Object[] list) throws InterruptedException {

	    for (int i = 0; i < list.length; i++) {
	      add(list[i]);
	    }
	  }

	  public synchronized Object remove() throws InterruptedException {

	    waitWhileEmpty();

	    Object obj = queue[tail];

	    queue[tail] = null;

	    tail = (tail + 1) % capacity;
	    size--;

	    notifyAll(); 

	    return obj;
	  }

	  public synchronized Object[] removeAll() throws InterruptedException {

	     Object[] list = new Object[size]; 

	    for (int i = 0; i < list.length; i++) {
	      list[i] = remove();
	    }

	    return list;
	  }

	  public synchronized Object[] removeAtLeastOne() throws InterruptedException {

	    waitWhileEmpty(); 
	    return removeAll();
	  }

	  public synchronized boolean waitUntilEmpty(long msTimeout)
	      throws InterruptedException {

	    if (msTimeout == 0L) {
	      waitUntilEmpty(); 
	      return true;
	    }

	    long endTime = System.currentTimeMillis() + msTimeout;
	    long msRemaining = msTimeout;

	    while (!isEmpty() && (msRemaining > 0L)) {
	      wait(msRemaining);
	      msRemaining = endTime - System.currentTimeMillis();
	    }

	    return isEmpty();
	  }

	  public synchronized void waitUntilEmpty() throws InterruptedException {

	    while (!isEmpty()) {
	      wait();
	    }
	  }

	  public synchronized void waitWhileEmpty() throws InterruptedException {

	    while (isEmpty()) {
	      wait();
	    }
	  }

	  public synchronized void waitUntilFull() throws InterruptedException {

	    while (!isFull()) {
	      wait();
	    }
	  }

	  public synchronized void waitWhileFull() throws InterruptedException {

	    while (isFull()) {
	      wait();
	    }
	  }
	}
	           