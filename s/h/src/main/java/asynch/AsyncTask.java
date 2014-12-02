package asynch;

import java.util.ArrayList;
import java.util.List;

public abstract class AsyncTask<T> implements Runnable {

	private List<AsyncCallback<T>> callbacks = new ArrayList<AsyncCallback<T>>();
	private List<AsyncErrback> errbacks = new ArrayList<AsyncErrback>();

	private boolean isTaskComplete;
	private List<Exception> exceptions = new ArrayList<Exception>();
	private T result;

	// override this method to create your async task.
	public abstract T executeTask() throws Exception;

	public AsyncTask() {
		// default constructor
	}

	public AsyncTask(AsyncCallback<T> callback) {
		callbacks.add(callback);
	}

	public AsyncTask(AsyncCallback<T> callback, AsyncErrback errback) {
		callbacks.add(callback);
		errbacks.add(errback);
	}

	public void run() {
		// execute the main task.
		try {
			result = executeTask(); 
			isTaskComplete = true;
			// execute callbacks
			
			for (AsyncCallback<T> callback : callbacks) {
				try {
					callback.callback(result);
					
				} catch (Exception error) {
					exceptions.add(error);
				}
				
			}
		} catch (Exception error) {
			exceptions.add(error);
		}

		// execute errbacks
		for (AsyncErrback errback : errbacks) {
			for (Exception exception : exceptions) {
				// error back is invoked once per every exception
				errback.errback(exception);
			}
		}
	}

	public boolean isTaskComplete() {
		return isTaskComplete;
	}

	public void registerCallback(AsyncCallback<T> callback) {
		callbacks.add(callback);
	}

	public void registerErrback(AsyncErrback errback) {
		errbacks.add(errback);
	}

	public List<AsyncCallback<T>> getCallbacks() {
		return callbacks;
	}

	public List<AsyncErrback> getErrBacks() {
		return errbacks;
	}

}
