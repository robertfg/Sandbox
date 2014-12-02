package sorting;

import java.util.Arrays;
import java.util.Comparator;

public class StringSortUsingComparator {

	public static void main(String args[]) {
		// Array with address to sort
		String[][] addressArray = new String[][] {
				{ "E", "abc st", "Sunshine", "FL" },
				{ "S", "abc st", "Sunrise", "FL" },
				{ "E", "abd st", "Cleveland", "OH" },
				{ "N", "aab st", "Dallas", "TX" },
				{ "W", "xyz st", "San Antonio", "TX" } };

		// Sort the array by city name or column 3
		Arrays.sort(addressArray, new ColumnComparator(2));

		// Print the sorted array
		for (int i = 0; i < addressArray.length; i++) {
			String[] row = addressArray[i];
			for (int j = 0; j < row.length; j++) {
				System.out.print(row[j] + " | ");
			}
			System.out.print("\n");
		}
	}
}

// Class that extends Comparator
class ColumnComparator implements Comparator {
	int columnToSort;

	ColumnComparator(int columnToSort) {
		this.columnToSort = columnToSort;
	}

	// overriding compare method
	public int compare(Object o1, Object o2) {
		String[] row1 = (String[]) o1;
		String[] row2 = (String[]) o2;
		// compare the columns to sort
		return row1[columnToSort].compareTo(row2[columnToSort]);
	}
}
