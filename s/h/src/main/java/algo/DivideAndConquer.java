package algo;

public class DivideAndConquer {

	static int sum = 0;

	public static void main(String[] args) {
		int[] numbers = { 2, 2, 2, 2, 2, 2, 2, 2 };
		int amount = 0;
		amount = sumArray(0, numbers.length, numbers);
		System.out.print(amount);
	
	/** Do the mergeSort **/
	
	
	}

	
	
	public static int sumArray(int first, int last, int[] A) {
		int index = last - first;
		if (index == 1) {
			return sum;
		} else if (index <= 4 && index > 1) {
			for (int i = first; i < last; i++) {
				sum += A[i];
			}
			return sum;
		}
		return (sumArray(first, last / 2, A) + sumArray(last / 2, A.length, A));
	}
	
	
	/**
	 * Merge sort is an algorithm with O(n.log(n)) timing complexity for all cases.
	 *  This algorithm is a divide and conquer algorithm. 
	 *  Merge sort has two parts which comparison and merging part.
	 * @param data
	 * @return
	 */
	public static int[] mergeSort(int[] data) {
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
			L = mergeSort(L);
			R = mergeSort(R);
			sorted = merge(L, R);
			return sorted;
		}
	}
	public static int[] merge(int[] L, int[] R) {
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

	
}
