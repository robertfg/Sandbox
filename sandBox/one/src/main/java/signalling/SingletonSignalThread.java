package signalling;

public class SingletonSignalThread {

	
	private boolean wait;

	public boolean isWait() {
		return wait;
	}

	public synchronized void setWait(boolean wait) {
		this.wait = wait;
	}
	
	
	
	
}
