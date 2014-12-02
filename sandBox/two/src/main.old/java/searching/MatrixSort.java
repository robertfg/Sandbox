package searching;

public class MatrixSort {

	public static void main(String args[]) {
		int[][] M = { { 1, 2, 3, 4 }, { 2, 3, 4, 5 }, { 5, 6, 7, 8 },
				{ 6, 7, 8, 5 }, { 3, 7, 9, 3 } };
		for (int r = 0; r < 5; r++) {
			for (int c = 0; c < 4; c++) {
				System.out.print(M[r][c] + "\t ");
			}
			System.out.println();
		}
		for (int r = 0; r < 5; r++) {
			int maxEle = M[r][0];
			for (int c = 1; c < 4; c++) {
				if (M[r][c] > maxEle) {
					int temp = maxEle;
					maxEle = M[r][c];
					M[r][c] = temp;
				}
			}
			System.out.println("Maximum row " + (r + 1) + "element is= "
					+ maxEle);
		}
	}
}