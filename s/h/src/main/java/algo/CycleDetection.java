package algo;

public class CycleDetection {
	//Pollard's rho algorithm
	//http://en.wikipedia.org/wiki/Pollard%27s_rho_algorithm
	
	//Floyd's Tortoise and the Hare algorithm Cycle detection
	//http://en.wikipedia.org/wiki/Cycle_detection#Brent.27s_algorithm
	
	//http://www.siafoo.net/algorithm/11
	
	
	//http://stackoverflow.com/questions/2663115/how-to-detect-a-loop-in-a-linked-list
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	class Node {
	    Node next;
	    // some user data
	}
	

	/**
	 * 	Here's a refinement of the Fast/Slow solution, which correctly handles odd length lists and improves clarity.
	 */
	boolean floydFasLoop(Node first) {
	    Node slow = first;
	    Node fast = first;

	    while(fast != null && fast.next != null) {
	        slow = slow.next;          // 1 hop
	        fast = fast.next.next;     // 2 hops 

	        if(slow == fast)  // fast caught up to slow, so there is a loop
	            return true;
	    }
	    return false;  // fast reached null, so the list terminates
	}
	
	/**
	 *  Brent
	 * //http://www.siafoo.net/algorithm/11
	 * @param root
	 * @return
	 */
	public static boolean hasLoop(Node root){
	    if(root == null) return false;

	    Node slow = root, fast = root;
	    int taken = 0, limit = 2;

	    while(slow.next != null && fast.next != null){
	        fast = fast.next;
	        taken++;
	        if(slow == fast) return true;

	        if(taken == limit){
	            taken = 0;
	            limit <<= 1;    // equivalent to limit *= 2;
	            slow = fast;    // teleporting the turtle (to the hare's position) 
	        }
	    }
	    return false;
	}

}
