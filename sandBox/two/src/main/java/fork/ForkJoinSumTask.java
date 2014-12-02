package fork;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinSumTask {

	Random random = new Random();

	public void fillArray(int[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = array[i] = random.nextInt(10000);
		}
	}

	public static void main(String[] args) {
		ForkJoinSumTask sum = new ForkJoinSumTask();
		int[] array = new int[200000000];
		sum.fillArray(array);

		long count;
		long start1;

		// Sequential process to get the sum of the elements in array
		for (int j = 0; j < 20; j++) {
			count = 0;
			start1 = System.currentTimeMillis();
			for (long i = 0; i < (long) array.length; i++) {
				count = (count + array[(int) i]);
			}

			System.out.println("Addition Result: " + count);
			System.out.println("Sequential processing time: "
					+ (System.currentTimeMillis() - start1) + " ms");

		}
		System.out.println("Parallel processing time");
		System.out.println("Number of processors available: "
				+ Runtime.getRuntime().availableProcessors());

		ForkJoinPool fjpool = new ForkJoinPool(Runtime.getRuntime()
				.availableProcessors());
		// Default parallelism level
		// =
		// Runtime.getRuntime().availableProcessors()
		long start2;

		for (int i = 0; i < 20; i++) {
			RecursiveSumTask task = new RecursiveSumTask(array, 0, array.length);
			start2 = System.currentTimeMillis();

			System.out.println("Addition Result: " + fjpool.invoke(task));

			System.out.println("Parallel processing time: "
					+ (System.currentTimeMillis() - start2) + " ms");
		}

		System.out
				.println("Number of steals: " + fjpool.getStealCount() + "\n");
	}
}

class RecursiveSumTask extends RecursiveTask<Long> {
	private static final long serialVersionUID = 1L;
	final int low;
	final int high;
	private int[] array;
	final int splitSize = 100000000; // Some threshold size to spit the task

	RecursiveSumTask(int[] array, int from, int to) {
		this.low = from;
		this.high = to;
		this.array = array;
	}

	@Override
	protected Long compute() {
		long count = 0L;
		List<RecursiveTask<Long>> forks = new ArrayList<>();

		if (high - low > splitSize) {
			// task is huge so divide in half
			int mid = (low + high) / 2;

			// Divided the given task into task1 and task2
			RecursiveSumTask task1 = new RecursiveSumTask(array, low, mid);
			forks.add(task1);
			task1.fork();

			RecursiveSumTask task2 = new RecursiveSumTask(array, mid, high);
			forks.add(task2);
			task2.fork();

		} else {
			// Calculating sum of the given array range
			for (int i = (int) low; i < high; i++) {
				count = count + array[i];
			}
		}

		// Waiting for the result
		for (RecursiveTask<Long> task : forks) {
			count = count + task.join();
		}

		return count;
	}
}