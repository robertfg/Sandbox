package divideAndCounquer;

import java.util.Random;

public class ThreadedNumberAggregator {

	private static final Random RAND = new Random(42); // random number
														// generator

	public static void main(String[] args) throws Throwable {
		int LENGTH = 1000; // initial length of array to sort
		int RUNS = 22; // how many times to grow by 2?

		for (int i = 1; i <= RUNS; i++) {
			int[] a = createRandomArray(LENGTH);

			// run the algorithm and time how long it takes
			long startTime1 = System.currentTimeMillis();
			int total = 0;
			for (int j = 1; j <= 100; j++) {
				total = sum3(a, 24);
			}
			long endTime1 = System.currentTimeMillis();

			int correct = sum(a);
			if (total != correct) {
				throw new RuntimeException("wrong sum: " + total + " vs. "
						+ correct);
			}

			System.out.printf("%10d elements  =>  %6d ms \n", LENGTH, endTime1
					- startTime1);
			LENGTH *= 2; // double size of array for next time
		}
	}

	// Computes the total sum of all elements of the given array.
	public static int sum(int[] a) {
		int result = 0;
		for (int i = 0; i < a.length; i++) {
			result += a[i];
		}
		return result;
	}

	// Computes the total sum of all elements of the given array.
	// This is a parallel version that uses 2 threads.
	// It will run almost twice as fast on a 2-CPU (or 2-core) machine.
	public static int sum2(int[] a) {
		// create two "summers" to run as separate threads
		ThreadAggregator leftSummer = new ThreadAggregator(a, 0, a.length / 2);
		ThreadAggregator rightSummer = new ThreadAggregator(a, a.length / 2,
				a.length);
		Thread leftThread = new Thread(leftSummer);
		Thread rightThread = new Thread(rightSummer);

		// run the threads
		leftThread.start();
		rightThread.start();

		// wait for the threads to finish
		try {
			leftThread.join();
			rightThread.join();
		} catch (InterruptedException ie) {
		}

		// combine the results of the two threads
		int left = leftSummer.getSum();
		int right = rightSummer.getSum();
		return left + right;
	}

	// Computes the total sum of all elements of the given array.
	// This is a parallel version that can use any number of threads.
	// It will can make use of as many cores/CPUs as you want to give it.
	public static int sum3(int[] a, int threadCount) {
		int len = (int) Math.ceil(1.0 * a.length / threadCount);
		ThreadAggregator[] summers = new ThreadAggregator[threadCount];
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			summers[i] = new ThreadAggregator(a, i * len, Math.min((i + 1)
					* len, a.length));
			threads[i] = new Thread(summers[i]);
			threads[i].start();
		}
		try {
			for (Thread t : threads) {
				t.join();
			}
		} catch (InterruptedException ie) {
		}

		int total = 0;
		for (ThreadAggregator summer : summers) {
			total += summer.getSum();
		}
		return total;
	}

	// helper method to compute sum of array a, indexes [min .. max).
	public static int sumRange(int[] a, int min, int max) {
		int result = 0;
		for (int i = min; i < max; i++) {
			result += a[i];
		}
		return result;
	}

	// Creates an array of the given length, fills it with random
	// non-negative integers, and returns it.
	public static int[] createRandomArray(int length) {
		int[] a = new int[length];
		for (int i = 0; i < a.length; i++) {
			a[i] = RAND.nextInt(50);
		}
		return a;
	}
}