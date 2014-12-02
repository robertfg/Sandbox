package sorting;

import java.util.Arrays;
import java.util.Random;

public class SortingUtil {

	public static void main(String[] args) {
		
		long start = System.currentTimeMillis();
		long end   = 0;
		SortingUtil sort = new SortingUtil();
		
		int	num[] =sort.generateRandomArr(10000000);
		num = sort.mergeSort(num);
		System.out.println( "mergesort total processing time:"  + ( System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		num =sort.generateRandomArr(20000000);
		num = sort.mergeSort(num);
		System.out.println( "mergesort total processing time:"  + ( System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		num =sort.generateRandomArr(30000000);
		num = sort.mergeSort(num);
		System.out.println( "mergesort total processing time:"  + ( System.currentTimeMillis() - start));
		
	
		System.out.println("-----------------------------------------------------------------------------");
		start = System.currentTimeMillis();
		num =sort.generateRandomArr(10000000);
		num = sort.quickSort(num);
		System.out.println( "quickSort total processing time:"  + ( System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		num =sort.generateRandomArr(20000000);
		num = sort.quickSort(num);
		System.out.println( "quickSort total processing time:"  + ( System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		num =sort.generateRandomArr(30000000);
		num = sort.quickSort(num);
		System.out.println( "quickSort total processing time:"  + ( System.currentTimeMillis() - start));
		
		
		
		
		start = System.currentTimeMillis();
		
		Arrays.sort(sort.generateRandomArr(30000000));
		
		System.out.println( "Arrays.sort total processing time:"  + ( System.currentTimeMillis() - start));
	
		
		

	}

	/**
	 *  Selection sort is a comparison sort with O(n2) timing complexity
	 *  making in-efficient on large sets. In this algorithm the element on active position (say ith position)
	 *  is compared with other positions (say i+1th to nth position) and swaps if it’s larger than the compared element.
	 * @param data
	 * @return
	 */
	public  int[] selectionSort(int[] data) {
		int lenD = data.length;
		int j = 0;
		int tmp = 0;
		for (int i = 0; i < lenD; i++) {
			j = i;
			for (int k = i; k < lenD; k++) {
				if (data[j] > data[k]) {
					j = k;
				}
			}
			tmp = data[i]; //current data
			data[i] = data[j]; //swap data
			data[j] = tmp;
			
		}
		return data;
	}

	/**
	 * Insertion sort is a comparison sort algorithm which works similar 
	 * to the way we arrange the cards in a hand. We take one element at a time,
	 *  starts compare from one end and them place them in between the card lesser and greater than it.
	 * @param data
	 * @return
	 */
	public int[] insertionSort(int[] data) {
		int len = data.length;
		int key = 0;
		int i = 0;
		for (int j = 1; j < len; j++) {
			key = data[j];
			i = j - 1;
			while (i >= 0 && data[i] > key) {
				data[i + 1] = data[i];
				i = i - 1;
				data[i + 1] = key;
			}
		}
		return data;
	}

	/**
	 * Bubble sort is a simple algorithm which compares neighbouring elements and swaps them if they are not in the order
	 * @param data
	 * @return
	 */
	public int[] bubbleSort(int[] data) {
		int lenD = data.length;
		int tmp = 0;
		for (int i = 0; i < lenD; i++) {
			for (int j = (lenD - 1); j >= (i + 1); j--) {
				if (data[j] < data[j - 1]) {
					tmp = data[j];
					data[j] = data[j - 1];
					data[j - 1] = tmp;
				}
			}
		}
		return data;
	}

	/**
	 * Quick sort is an algorithm with O(n.log(n)) average timing complexity.
	 *  This algorithm is a recursive algorithm. In here it select an element (randomly or middle element of the array)
	 *  and put elements to left to it if its lesser that it or to right side otherwise till all elements are sorted.
	 * @param data
	 * @return
	 */
	public int[] quickSort(int[] data) {
		int lenD = data.length;
		int pivot = 0;
		int ind = lenD / 2;
		int i, j = 0, k = 0;
		if (lenD < 2) {
			return data;
		} else {
			int[] L = new int[lenD];
			int[] R = new int[lenD];
			int[] sorted = new int[lenD];
			pivot = data[ind];
			for (i = 0; i < lenD; i++) {
				if (i != ind) {
					if (data[i] < pivot) {
						L[j] = data[i];
						j++;
					} else {
						R[k] = data[i];
						k++;
					}
				}
			}
			int[] sortedL = new int[j];
			int[] sortedR = new int[k];
			System.arraycopy(L, 0, sortedL, 0, j);
			System.arraycopy(R, 0, sortedR, 0, k);
			sortedL = quickSort(sortedL);
			sortedR = quickSort(sortedR);
			System.arraycopy(sortedL, 0, sorted, 0, j);
			sorted[j] = pivot;
			System.arraycopy(sortedR, 0, sorted, j + 1, k);
			return sorted;
		}
	}
	
	
	/**
	 * Merge sort is an algorithm with O(n.log(n)) timing complexity for all cases.
	 *  This algorithm is a divide and conquer algorithm. 
	 *  Merge sort has two parts which comparison and merging part.
	 * @param data
	 * @return
	 */
	public int[] mergeSort(int[] data) {
		int lenD = data.length;
		if (lenD <= 1) {
			return data;
		} else {
			int[] sorted = new int[lenD];
			int middle = lenD / 2;
			int rem = lenD - middle;
			int[] L = new int[middle];
			int[] R = new int[rem];
			System.arraycopy(data, 0, L, 0, middle);
			System.arraycopy(data, middle, R, 0, rem);
			L = this.mergeSort(L);
			R = this.mergeSort(R);
			sorted = merge(L, R);
			return sorted;
		}
	}

	
	public int[] merge(int[] L, int[] R) {
		int lenL = L.length;
		int lenR = R.length;
		int[] merged = new int[lenL + lenR];
		int i = 0;
		int j = 0;
		while (i < lenL || j < lenR) {
			if (i < lenL & j < lenR) {
				if (L[i] <= R[j]) {
					merged[i + j] = L[i];
					i++;
				} else {
					merged[i + j] = R[j];
					j++;
				}
			} else if (i < lenL) {
				merged[i + j] = L[i];
				i++;
			} else if (j < lenR) {
				merged[i + j] = R[j];
				j++;
			}
		}
		return merged;
	}

	/**
	 * Shell sort is a generalised form of insertion sort algorithm which improved the performance of insertion sort.
	 * @param data
	 * @return
	 */
	public int[] shellSort(int[] data) {
		int lenD = data.length;
		int inc = lenD / 2;
		while (inc > 0) {
			for (int i = inc; i < lenD; i++) {
				int tmp = data[i];
				int j = i;
				while (j >= inc && data[j - inc] > tmp) {
					data[j] = data[j - inc];
					j = j - inc;
				}
				data[j] = tmp;
			}
			inc = (inc / 2);
		}
		return data;
	}
	
	
	public int[] generateRandomArr(int size){
	
		int[] a = new int[size];
		Random rg = new Random();
		for (int i = 0; i < size; i++)
		{
		    int r = rg.nextInt(i+1);
		    a[i] = a[r];
		    a[r] = i+1;
		}
		
		return a;
	}
	
	
	
}
