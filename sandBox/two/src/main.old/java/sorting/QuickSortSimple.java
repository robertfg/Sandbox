package sorting;

public class QuickSortSimple {

	public static void main(String[] args) {
		int[] intArray = { 1, 9, 2, 8, 3, 7, 4, 6, 5 };
		for (int i : intArray) {
			System.out.println(i);
		}
		quickSort(intArray);
		for (int i : intArray) {
			System.out.println(i);
		}
	}

	public static void quickSort(int[] intArray) {
		recQuickSort(intArray, 0, intArray.length - 1);
	}

	private static void recQuickSort(int[] intArray, int left, int right) {
		if (right - left <= 0)
			return;
		else {
			int pivot = intArray[right];

			int partition = partitionIt(intArray, left, right, pivot);
			recQuickSort(intArray, left, partition - 1);
			recQuickSort(intArray, partition + 1, right);
		}
	}

	private static int partitionIt(int[] intArray, int left, int right,
			int pivot) {
		int leftPtr = left - 1;
		int rightPtr = right;
		while (true) {
			while (intArray[++leftPtr] < pivot)
				;

			while (rightPtr > 0 && intArray[--rightPtr] > pivot)
				;

			if (leftPtr >= rightPtr)
				break;
			else
				swap(intArray, leftPtr, rightPtr);
		}
		swap(intArray, leftPtr, right);
		return leftPtr;
	}

	private static void swap(int[] intArray, int dex1, int dex2) {
		int temp = intArray[dex1];
		intArray[dex1] = intArray[dex2];
		intArray[dex2] = temp;
	}
}
