package sorting;

import java.util.Arrays;
import java.util.Random;

public class AnotherSorting {
	
	private static final Random RAND = new Random(42);   // random number generator

	public static void main(String[] args) {
		int LENGTH = 1000;   // initial length of array to sort
		int RUNS   =  8;   // how many times to grow by 2?

		for (int i = 0; i < RUNS; i++) {
			int[] a = createRandomArray(LENGTH);

			// perform a sort and time how long it takes
			long startTime1 = System.currentTimeMillis();
			mergeSort(a);
			long endTime1 = System.currentTimeMillis();
			
			if (!isSorted(a)) {
				throw new RuntimeException("not sorted afterward: " + Arrays.toString(a));
			}

			System.out.printf("%10d elements  =>  %6d ms \n", LENGTH, endTime1 - startTime1);
			LENGTH *= 2;   // double size of array for next time
		}
	}
	
	// Arranges the elements of the given array into sorted order
	// using the "merge sort" algorithm, which splits the array in half,
	// recursively sorts the halves, then merges the sorted halves.
	// It is O(N log N) for all inputs.
	public static void mergeSort(int[] a) {
		if (a.length >= 2) {
			// split array in half
			int[] left  = Arrays.copyOfRange(a, 0, a.length / 2);
			int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);
			
			// sort the halves
			mergeSort(left);
			mergeSort(right);
			
			// merge them back together
			int i1 = 0;
			int i2 = 0;
			for (int i = 0; i < a.length; i++) {
				if (i2 >= right.length ||
						(i1 < left.length && left[i1] < right[i2])) {
					a[i] = left[i1];
					i1++;
				} else {
					a[i] = right[i2];
					i2++;
				}
			}
		}
	}
	
	
	// Arranges the elements of the given array into sorted order
	// using the "shell sort" algorithm, which creates slices of
	// the array separated by a given gap and insertion-sorts by
	// that gap, eventually ending in a normal insertion sort.
	// It is O(N^1.25) on average.
	public static void shellSort(int[] a) {
		for (int gap = a.length / 2; gap >= 1; gap = gap / 2) {
			// insertion sort on every gap'th element
			for (int i = gap; i < a.length; i++) {
				int temp = a[i];
				int j = i;
				while (j >= gap && a[j - gap] > temp) {
					a[j] = a[j - gap];
					j -= gap;
				}
				a[j] = temp;
			}
		}
	}
	
	
	
	// Arranges the elements of the given array into sorted order
	// using the "insertion sort" algorithm, which manages a sorted
	// prefix of the array, and one at a time, shifts each next element
	// to be in the right ordered place within that sorted prefix
	// until the entire array is sorted.
	// It is O(N^2) but faster (O(N)) for sorted inputs.
	public static void insertionSort(int[] a) {
		for (int i = 1; i < a.length; i++) {
			// move a[pass] into its proper place,
			// assuming that a[0]..a[pass-1] are sorted
			int temp = a[i];
			int j = i;
			while (j >= 1 && a[j - 1] > temp) {
				a[j] = a[j - 1];
				j--;
			}
			a[j] = temp;
		}
	}
	
	// Arranges the elements of the given array into sorted order
	// using the "selection sort" algorithm, which makes sweeps over
	// the array, finding the i'th smallest element each time,
	// and swapping it to be stored at index i.
	// It is O(N^2) for all inputs.
	public static void selectionSort(int[] a) {
		for (int pass = 0; pass < a.length; pass++) {
			// figure out what should go into a[pass]
			int min = pass;
			for (int j = pass + 1; j < a.length; j++) {
				if (a[j] < a[min]) {
					min = j;
				}
			}
			
			swap(a, pass, min);
		}
	}
	
	// Arranges the elements of the given array into sorted order
	// using the "bubble sort" algorithm, which makes sweeps over
	// the array and swaps neighbors that are out of order.
	// It is O(N^2) for all inputs.
	public static void bubbleSort(int[] a) {
		for (int pass = 0; pass < a.length; pass++) {
			// make a sweep
			boolean changed = false;
			for (int i = 0; i < a.length - 1 - pass; i++) {
				if (a[i] > a[i + 1]) {
					swap(a, i, i + 1);
					changed = true;
				}
			}
			if (!changed) {
				return;
			}
		}
	}
	
	// Arranges the elements of the given array into sorted order
	// using the "bogo sort" algorithm.
	// It is O(N!) on average.  (very bad!)
	public static void bogoSort(int[] a) {
		while (!isSorted(a)) {
			shuffle(a);
		}
	}
	
	// Swaps the values at the two given indexes in the given array.
	private static final void swap(int[] a, int i, int j) {
		if (i != j) {
			int temp = a[i];
			a[i] = a[j];
			a[j] = temp;
		}
	}
	
	// Randomly rearranges the elements of the given array.
	private static void shuffle(int[] a) {
		for (int i = 0; i < a.length; i++) {
			// move element i to a random index in [i .. length-1]
			int randomIndex = (int) (Math.random() * a.length - i);
			swap(a, i, i + randomIndex);
		}
	}
	
	// Returns true if the given array is in sorted ascending order.
	private static boolean isSorted(int[] a) {
		for (int i = 0; i < a.length - 1; i++) {
			if (a[i] > a[i + 1]) {
				return false;
			}
		}
		return true;
	}
	
	

	// Creates an array of the given length, fills it with random
	// non-negative integers, and returns it.
	public static int[] createRandomArray(int length) {
		int[] a = new int[length];
		for (int i = 0; i < a.length; i++) {
			a[i] = RAND.nextInt(1000000000);
		}
		return a;
	}

	// Creates an array of the given length, fills it with ordered
	// non-negative integers, and returns it.
	public static int[] createAscendingArray(int length) {
		int[] a = new int[length];
		for (int i = 0; i < a.length; i++) {
			a[i] = i;
		}
		return a;
	}

	// Creates an array of the given length, fills it with reverse-ordered
	// non-negative integers, and returns it.
	public static int[] createDescendingArray(int length) {
		int[] a = new int[length];
		for (int i = 0; i < a.length; i++) {
			a[i] = a.length - 1 - i;
		}
		return a;
	}
}