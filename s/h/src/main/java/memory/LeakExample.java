package memory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

public class LeakExample {
	static Vector myVector = new Vector();
	static HashSet pendingRequests = new HashSet();

	public void slowlyLeakingVector(int iter, int count) {
		for (int i=0; i<iter; i++) {
			for (int n=0; n<count; n++) {
				myVector.add(Integer.toString(n+i));
			}
			for (int n=count-1; n>0; n--) {
				// Oops, it should be n>=0
				myVector.removeElementAt(n);
			}
		}
	}

	public void leakingRequestLog(int iter) {
		Random requestQueue = new Random();
		for (int i=0; i<iter; i++) {
			int newRequest = requestQueue.nextInt();
			pendingRequests.add(new Integer(newRequest));
			// processed request, but forgot to remove it
			// from pending requests
		}
	}

	public void noLeak(int size) {
		HashSet tmpStore = new HashSet();
		for (int i=0; i<size; ++i) {
			String leakingUnit = new String("Object: " + i);
			tmpStore.add(leakingUnit);
		}
		// Though highest memory allocation happens in this
		// function, but all these objects get garbage
		// collected at the end of this method, so no leak.
	}

	public static void main(String[] args) throws IOException {
		LeakExample javaLeaks = new LeakExample();
		for (int i=0; true; i++) {
			//try { // sleep to slow down leaking process
			//	Thread.sleep(1000);
			//} catch (InterruptedException e) { /* do nothing */ }
			System.out.println("Iteration: " + i);
			//javaLeaks.slowlyLeakingVector(1000,10);
			javaLeaks.leakingRequestLog(5000);
			//javaLeaks.noLeak(100000);
		}
	}
}