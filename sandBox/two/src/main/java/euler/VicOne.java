package euler;

import java.util.Random;

public class VicOne {

	public static void main(String[] args) {
		int dim =15;
		
		int[][] numMatrix = generateSampleData();
		
		// get the max
		System.out.println("");
		System.out.println("");
		System.out.println("Possible paths:");
		int maxTotal = 0;
		String maxTotalPositions = "";
		
		for (int j=0; j<dim; j++) { // column
			
			int numOfRowLoop = 1;
			
			if (j > 0) {
				numOfRowLoop = dim - j;
			}
		
			for (int k=1; k<=numOfRowLoop; k++) {
				int currentTotal = 0;
				String currentTotalPositions = "";
				
				for (int i=0; i<dim; i++) { // row
					int check = dim - k;
					if (i >= check || j == 0) {
						currentTotal = add(currentTotal, numMatrix[i][j]);
						currentTotalPositions = add(currentTotalPositions, numMatrix[i][j]);
					}
					else {
						if (j-i > 1) {
							currentTotal = add(currentTotal, numMatrix[i][i]);
							currentTotalPositions = add(currentTotalPositions, numMatrix[i][i]);
						}
						else {
							currentTotal = add(currentTotal, numMatrix[i][j-1]);
							currentTotalPositions = add(currentTotalPositions, numMatrix[i][j-1]);
						}
					}
				}
				
				System.out.println( currentTotalPositions );
				
				if (currentTotal > maxTotal) {
					maxTotal = currentTotal;
					maxTotalPositions = currentTotalPositions;
				}
			
			}
		}
		
		System.out.println("maxTotal: " + maxTotal);
		System.out.println("maxTotalPositions: " + maxTotalPositions);

	}
	
	private static int add(int num1, int num2) {
		return num1 + num2;
	}
	
	private static String add(String num1, int num2) {
		//System.out.print(num2 + "    ");
		return num1 + " " + num2;
	}
	
	private static int[][] generateTriangle(int depth){
		
		int[][] triangle = new int[depth][depth];
		Random rnd = new Random();
		for (int i=0; i<depth; i++) { // row
			for (int j=0; j<depth; j++) { // column
				int rndNum = rnd.nextInt(90) + 10;
				if (j <= i) {
					triangle[i][j] = rndNum;
					System.out.print(rndNum + "    ");
				}
				else {
					System.out.println("");
					break;
				}
			}
		}
		return triangle;
		
	}
	private static int[][] generateSampleData() {
		
		int dim = 15;
		int[][] numMatrix = new int[dim][dim];
		
		String sb = "" +
				"75" + "\n" +
				"95  64" + "\n" +
				"17  47  82" + "\n" +
				"18  35  87  10" + "\n" +
				"20  04  82  47  65" + "\n" +
				"19  01  23  75  03  34" + "\n" +
				"88  02  77  73  07  63  67" + "\n" +
				"99  65  04  28  06  16  70  92" + "\n" +
				"41  41  26  56  83  40  80  70  33" + "\n" +
				"41  48  72  33  47  32  37  16  94  29" + "\n" +
				"53  71  44  65  25  43  91  52  97  51  14" + "\n" +
				"70  11  33  28  77  73  17  78  39  68  17  57" + "\n" +
				"91  71  52  38  17  14  91  43  58  50  27  29  48" + "\n" +
				"63  66  04  68  89  53  67  30  73  16  69  87  40  31" + "\n" +
				"04  62  98  27  23  09  70  98  73  93  38  53  60  04  23";
		 
	/*	sb = "" +
				"3" + "\n" +
				"7  4" + "\n" +
				"2  4  6" + "\n" +
				"8  5  9  3";*/
		
				
		//System.out.println(sb);
		
		String[] lines = sb.split("\n", -2);
		int i = 0;
		for (String line: lines) {
			String[] data = line.split("  ", -2);
			for (int j=0; j<data.length; j++) {
				numMatrix[i][j] = Integer.valueOf(data[j]);
			}
			i++;
			System.out.println(line);
		}
		
		return numMatrix;
		
	}

}
