package sorting;

public class MergeSorterThread implements Runnable {
	private int[] a;
	private int threadCount;
	
	public MergeSorterThread(int[] a, int threadCount) {
		this.a = a;
		this.threadCount = threadCount;
	}
	
	public void run() {
		MergeSortThreaded.parallelMergeSort(a, threadCount);
	}
}