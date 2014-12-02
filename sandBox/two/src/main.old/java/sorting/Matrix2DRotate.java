package sorting;

public class Matrix2DRotate {

	static int[][] twoDimensionalArray = new int[][] { { 1, 2, 3, 4 },
			{ 5, 6, 7, 8 }, { 9, 10, 11, 12 }, { 13, 14, 15, 16 }

	};

	public static void main(String[] args) {

		System.out.println("\n\nINPUT ARRAY");

		printTwoDimensionalArray(twoDimensionalArray);

		System.out.print("\n\nROTATED WITH EXTRA SPACE:");

		int[][] rotatedArray = rotate2DArray(twoDimensionalArray);

		printTwoDimensionalArray(rotatedArray);

		System.out.print("\n\nROTATED WITH IN PLACE ROTATION:");

		rotatedArray = rotateInPlace(twoDimensionalArray, 4);

		printTwoDimensionalArray(rotatedArray);
	}

	/**
	 * @param array
	 *            - two dimensional array
	 * @return Array rotated to right using extra space.
	 */
	private static int[][] rotate2DArray(int[][] array) {
		long startTime = System.currentTimeMillis();
		int length = array[0].length;
		int[][] tempArray = new int[length][length];

		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				tempArray[i][j] = array[length - 1 - j][i];
			}

		}

		long endTime = System.currentTimeMillis();
		printTimeTaken(startTime, endTime);

		return tempArray;
	}

	private static void printTimeTaken(long startTime, long endTime) {

		// System.out.print("StartTime: " + startTime +" endTime: " + endTime);

		if (startTime <= 0 || endTime <= 0 || startTime > endTime) {
			return;
		}

		System.out.println("Took " + (endTime - startTime) + " ms");

	}

	/**
	 * This method rotates a two dimensional matrix in layers. Portion of this
	 * code is taken from Cracking The Coding Interview Book
	 * 
	 * @param matrix
	 *            - Two Dimensional Matrix
	 * @param size
	 *            - size of Matrix
	 * @return In Place Rotated Matrix
	 */
	public static int[][] rotateInPlace(int[][] matrix, int size) {
 
        long startTime = System.currentTimeMillis();
 
        for (int layer = 0; layer < size / 2; ++layer) {
 
            int first = layer;
            int last = size - 1 - layer;
 
            for (int i = first; i < last; ++i) {                
            	int offset = i - first;                             //Saves the top value               
            	int top = matrix[first][i];                                 // left -> top
            
                matrix[first][i] = matrix[last - offset][first];
 
                // bottom -> left
                matrix[last - offset][first] = matrix[last][last - offset];
 
                // right -> bottom
                matrix[last][last - offset] = matrix[i][last];
 
                // top -> right
                matrix[i][last] = top; // right             }
        }
        }
 
        long endTime = System.currentTimeMillis();
        printTimeTaken(startTime, endTime);
 
        return matrix;
    }

	/**
	 * This method prints the two dimensional array to System Console
	 */
	private static void printTwoDimensionalArray(int[][] intArray) {
		for (int i = 0; i < intArray[0].length; i++) {
			for (int j = 0; j < intArray[i].length; j++) {
				System.out.print(intArray[i][j] + "\t");
			}
			System.out.println();
		}

	}
}