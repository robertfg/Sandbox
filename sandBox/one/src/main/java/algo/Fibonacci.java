package algo;

public class Fibonacci {

	/**
	 * @param args
	 */
	
	
	  public static long fib(int n) {
	        if (n <= 1) return n;
	        else return fib(n-1) + fib(n-2);
	    }

	    public static void main(String[] args) {
	        int N = Integer.parseInt("100");
	        for (int i = 1; i <= N; i++)
	            System.out.println(i + ": " + fib(i));
	    }
}
