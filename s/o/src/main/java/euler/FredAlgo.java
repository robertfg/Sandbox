package euler;

import java.util.Arrays;


public class FredAlgo {

		int maxTotal  = 0;
		int maxTotal2 = 0;
		
		int[][] data;
		boolean allDone = false;
		int pass = 0;
	//	List<Integer> matrix = new ArrayList<Integer>();
		
		public FredAlgo(int[][] data){
			this.data = data;
		}
		
		public void process(){
			push(0,0,0,"");
			if(maxTotal2>maxTotal) maxTotal = maxTotal2;	
			
		}
		
		
		public void push(int row, int column, int value, String origin){
			
		System.out.println("------------------------------------START-LOOP-----------------------------------------------------------------------");	
		System.out.println("row:" + row +",col:" + column + ",value:" + value + ",data length:" + data.length);
           // if(pass>9) return; 		
			if (row >= data.length){ System.out.println("exit"); return;}
			
			int firstValue = value;
			int secondValue = value;
			
			int[] rowData = data[row];
			System.out.println("rowData:" + Arrays.toString(rowData) );
			int columnVal1 = 0;
			int columnVal2 = 0;
			
			int nextColumn = column+1;
			
			if (column < rowData.length){
				columnVal1 = rowData[column];
				System.out.println("Current Row:" + row + ",Current Column:" + column + ",columnVal1:" + columnVal1 
						+ ", firstValue:" + firstValue + ",(firstValue + columnVal1)=" + (firstValue + columnVal1) );
				firstValue += columnVal1;
			}
			
			if (column+1 < rowData.length){
				columnVal2 = rowData[nextColumn];
				System.out.println("Current Row:" + row + ",   Next Column:" + nextColumn + ",columnVal2:" + columnVal2 
						+ ",secondValue:" + secondValue + ",(secondValue + columnVal2)=" + (secondValue + columnVal2) );
				secondValue += columnVal2;
			}
			
			String s = origin + String.format("/[%s,%s-{%s:%s:%s},{%s:%s:%s}]", row, value, column, columnVal1, firstValue, nextColumn, columnVal2,secondValue);
			//System.out.println(s);
			
			push(row+1, column, firstValue, s);
			System.out.println("***************************************************************************************");
			System.out.println("Second PUSH row:" + (row + 1) +",col:" + (column + 1) + ",second value:" + secondValue);
			
			push(row+1, column+1, secondValue, s);
			
			if (firstValue > maxTotal) {
				maxTotal = firstValue;
			//	System.out.println(s);
			}
			if (secondValue > maxTotal2){
				maxTotal2 = secondValue;
				//System.out.println(s);
			}
	
			System.out.println("maxTotal=" + maxTotal + ",maxTotal2=" + maxTotal2 );
		    pass++;
		    
		}
		
		public int getMaxTotals(){
			return maxTotal;
		}
		public static void main(String[] args){
			
			int[][] triangle1 = {
					
					 {3},
					{7,4},
				   {2,4,6},
				  {8,5,9,3}
				   
			};

			int[][] triangle2 = {
					 {1},
					{2,3},
				   {4,5,6}
			};

			
			int[][] triangle3 = {
					{75},
					{95, 64},
					{17,  47,  82},
					{18,  35,  87,  10},
					{20,  04,  82,  47,  65},
					{19,  01,  23,  75,  3,  34},
					{88,  02,  77,  73,  7,  63,  67},
					{99,  65,  04,  28,  6,  16,  70,  92},
					{41,  41,  26,  56,  83,  40, 80,  70,  33},
					{41,  48,  72,  33,  47,  32,  37,  16,  94,  29},
					{53,  71,  44,  65,  25,  43,  91,  52,  97,  51,  14},
					{70,  11,  33,  28,  77,  73,  17,  78,  39,  68,  17,  57},
					{91,  71,  52,  38,  17,  14,  91,  43,  58,  50,  27,  29,  48,},
					{63,  66,  04,  68,  89,  53,  67,  30,  73,  16,  69,  87,  40,  31},
					{04,  62,  98,  27,  23,  9,  70,  98,  73,  93,  38,  53,  60,  04,  23}
							};
			//System.out.println("Test Data=" + Arrays.toString(triangle2));
			
			FredAlgo f = new FredAlgo(triangle3);
					f.process();
					
					System.out.println(f.getMaxTotals());
		}
		
}
