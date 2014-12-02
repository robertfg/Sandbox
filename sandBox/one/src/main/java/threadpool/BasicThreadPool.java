package threadpool;



	import java.util.LinkedList;


	/**
	 * Very basic implementation of a thread pool.
	 * 
	 * @author Rob Gordon.
	 */
	public class BasicThreadPool {
		
	    private BlockingQueue queue = new BlockingQueue();
	    private boolean closed = true;

	    private int poolSize = 3;
	    
	    
	    public void setPoolSize(int poolSize) {
	        this.poolSize = poolSize;
	    }
	    
	    public int getPoolSize() {
	        return poolSize;
	    }
	    
	    synchronized public void start() {
	        if (!closed) {
	            throw new IllegalStateException("Pool already started.");
	        }
	        closed = false;
	        for (int i = 0; i < poolSize; ++i) {
	            new PooledThread().start();
	        }
	    }

	    synchronized public void execute(Runnable job) {
	        if (closed) {
	            throw new PoolClosedException();
	        }
	        queue.enqueue(job);
	    }
	    
	    private class PooledThread extends Thread {
	        
	        public void run() {
	            while (true) {
	                Runnable job = (Runnable) queue.dequeue();
	                if (job == null) {
	                    break;
	                }
	                try {
	                    job.run();
	                } catch (Throwable t) {
	                    // ignore
	                }
	            }
	        }
	    }

	    public void close() {
	        closed = true;
	        queue.close();
	    }
	    
	    private static class PoolClosedException extends RuntimeException {
	        PoolClosedException() { 
	            super ("Pool closed.");
	        }
	    }
	}
	/*
	 * Copyright © 2004, Rob Gordon.
	 */

	/**
	 *
	 * @author Rob Gordon.
	 */
	 class BlockingQueue {

	  private final LinkedList list = new LinkedList();
	  private boolean closed = false;
	  private boolean wait = false;
	  
	  synchronized public void enqueue(Object o) {
	    if (closed) {
	      throw new ClosedException();
	    }
	    list.add(o);
	    notify();
	  }

	  synchronized public Object dequeue() {
	    while (!closed && list.size() == 0) {
	      try {
	        wait();
	      }
	      catch (InterruptedException e) {
	        // ignore
	      }
	    }
	    if (list.size() == 0) {
	      return null;
	    }
	    return list.removeFirst();
	  }
	  
	  synchronized public int size() {
	      return list.size();
	  }
	  
	  synchronized public void close() {
	    closed = true;
	    notifyAll();
	  }
	  
	  synchronized public void open() {
	      closed = false;
	  }
	  
	  public static class ClosedException extends RuntimeException {
	      ClosedException() {
	          super("Queue closed.");
	      }
	  }
	}

	   