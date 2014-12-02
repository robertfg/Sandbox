package euler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class AlgoOne {

	private static Map<Integer,List<Integer>> triangle = new HashMap<Integer,List<Integer>>();

	
	public static void main(String[] args) {
			
			
			int[][] triangle = new int[][]{
					    { 1 },
					   { 2,3 },
					  { 4, 5 ,6 },
					{ 7, 8 ,9,10 }
					};
	
			int[][] triangle1 = new int[][]{
				    { 1 },
				   { 2,3 },
				  { 4, 5 ,6 },
				{ 7, 8 ,9,10 }
				};
			
			int[][] triangle2 = {
					
					 {3},
					{7,4},
				   {2,4,6},
				  {8,5,9,3}
				   
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
					{04,  62,  98,  27,  23,  9,  70,  98,  73,  93,  38,  53,  60,  04,  23}};
					
			//AlgoOne.myAlgo(triangle3);
			
			TreeSet<String> execPath = new TreeSet<String>();
			
			AlgoOne.generateExecutionPath(triangle3, 0, 0, "", execPath );
			AlgoOne.getMaxSumUsingBottomUpApproach(triangle3);
			
			int maxSum = 0;
			String highestExecutionPath = "";
			for (String path : execPath) {
				int aggregate = aggreateArr(path); 
				if( aggregate > maxSum){
					maxSum = aggregate;
					highestExecutionPath = path;
				}
			}
			System.out.println( highestExecutionPath + "=" + maxSum );
			
			execPath = new TreeSet<String>();
			AlgoOne.generateExecutionPath(triangle2, 0, 0, "", execPath );
			AlgoOne.getMaxSumUsingBottomUpApproach(triangle2);
			
			
			maxSum = 0;
			highestExecutionPath = "";
			for (String path : execPath) {
				int aggregate = aggreateArr(path); 
				if( aggregate > maxSum){
					maxSum = aggregate;
					highestExecutionPath = path;
				}
			}
			System.out.println( highestExecutionPath + "=" + maxSum );
	}
	
   
   public static int aggreateArr(String path) {
	    String[] values =   path.split(";");
	    int sum = 0;
	    for (int i = 0; i < values.length; i++) {
		   	sum+= Integer.valueOf(values[i]);
		}
	   return sum;
   }
	
  
   public static String[] splitPath( String path, String delimeter ){
		return path.split(";");
   }
   
   public static void insertPath(String path, int depth, TreeSet<String> executionPath){
		if( AlgoOne.splitPath(path,";").length == depth){
    		executionPath.add( path ) ; 
    	}
	}
	
	public static void generateExecutionPath(int[][] triangle, int row, int col, String path, TreeSet<String> executionPath){
		    if(row >=triangle.length) {
		    	insertPath(path, triangle.length, executionPath);
		    	return ;
			}
		    path+=triangle[row][col] + ";";
			generateExecutionPath(triangle, row+1,col,path,executionPath);   // to get the left adjacent node
			generateExecutionPath(triangle, row+1,col+1,path,executionPath); // to get the right adjacent node
			insertPath(path, triangle.length, executionPath);
	}
	
	public static void getMaxSumUsingBottomUpApproach(int[][] triangle){
		
		for (int i = triangle.length -2; i >=0 ; i--) {
			
			for (int j = 0; j < triangle[i].length; j++) {
			    int curr = triangle[i][j];
				int left = triangle[i+1][j];
				int right = triangle[i+1][j+1];
				
				if( (curr+left) > (curr+right)  ){
					triangle[i][j] = (curr+left); 
				} else {
					triangle[i][j] = (curr+right);
				}
			}
		}

		System.out.println("highest number is:" + triangle[0][0]);
	}
}
