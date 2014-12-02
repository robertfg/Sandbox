package jpmorgan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Test1 {

	final static int TO_X1 = 0;
	final static int TO_Y1 = 1;
	final static int TO_X2 = 2;
	final static int TO_Y2 = 3;
	
	static int DIM = 0;
	static int MAX_TOTAL = 0;
	static String MAX_TOTAL_PATH = "";
	
	public static void main(String[] args) {
		// generate sample data
		int[][] numMatrix = generateSampleData();
		
		// get parent-child relationship
		System.out.println("parents: ");
		Map<String, int[]> adjacent = new LinkedHashMap<String, int[]>();
		
		for (int i=0; i< numMatrix.length -1; i++) { // row
			for (int j=0; j < numMatrix.length ; j++) { // column
				if (j <= i) {
					String key1 = i + "|" + j;
			
					int[] value1 = new int[4];
						  value1[TO_X1] = i+1;
						  value1[TO_Y1] = j;
						  
						  value1[TO_X2] = i+1;
						  value1[TO_Y2] = j+1;
					
					System.out.println(key1 + " --->" + Arrays.toString(value1));
					adjacent.put(key1, value1);
				}
				else {
					break;
				}
			}
		}
		
		
		// initialize container of all possible paths
		System.out.println("");
		List<String> path = new ArrayList<String>();
		List<String> prevPath = new ArrayList<String>();
		
		for (int i=0; i< numMatrix.length; i++) {
			prevPath.add("");
		}
		
		List<String> paths = new ArrayList<String>();
		
		// root parent
		String key = 0 + "|" + 0;
		
		// get all possible paths (recursive)
		getPaths(adjacent, key, path, prevPath, paths,numMatrix.length);
		
		// get total of each path and get the path with the highest total
		getMaxTotal(paths, numMatrix);
		System.out.println("maxTotal: " + MAX_TOTAL);
		System.out.println("maxTotalPath: " + MAX_TOTAL_PATH);
	}
	
	private static void getPaths(Map<String, int[]> adjacent, String key, List<String> path, List<String> prevPath, List<String> paths, int depth) {
		// get 2 children (adjacent) 
		int[] branch = adjacent.get(key);
		
		if (branch != null) {
			String key1 = branch[TO_X1] + "|" + branch[TO_Y1];
			path.add(key + "-" + key1);
			getPaths(adjacent, key1, path, prevPath, paths,depth);
			
			String key2 = branch[TO_X2] + "|" + branch[TO_Y2];
			path.add(key + "-" + key2);
			getPaths(adjacent, key2, path, prevPath, paths,depth);
		}
		else {
			//System.out.println("");
			reset(path, prevPath, paths,depth);
		}
	}
	
	private static void reset(List<String> path, List<String> prevPath, List<String> paths, int depth) {
		int i = 0;
		int pathSize = path.size();
		for (String str: path) {
			prevPath.set(((depth-1) - pathSize + i), str);
			i++;
		}
		String strs = "";
		int j = 0;
		for (String str: prevPath) {
			String[] strArr1 = str.split("-", -2);
			if (strArr1.length > 1) {
				strs = strs + strArr1[0] + " ";
				if (j == prevPath.size()-2) {
					strs = strs + strArr1[1];
				}
			}
			j++;
		}
		//System.out.println(strs);
		path.clear();
		paths.add(strs);
	}
	
	private static void getMaxTotal(List<String> paths, int[][] numMatrix) {
		for (String str: paths) {
			int total = 0;
			String[] strArr = str.split(" ", -2);
			for (String str1: strArr) {
				String[] strArr1 = str1.split("\\|", -2);
				total = total + numMatrix[Integer.valueOf(strArr1[0])][Integer.valueOf(strArr1[1])];
			}
			if (total > MAX_TOTAL) {
				MAX_TOTAL = total;
				MAX_TOTAL_PATH = str;
			}
		}
	}
	
	private static int[][] generateSampleData() {
		
		
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
		
		/*
		String sb = "" +
				"3" + "\n" +
				"7  4" + "\n" +
				"2  4  6" + "\n" +
				"8  5  9  3";
		*/
		
		 String sb2 = "" +
				"1" + "\n" +
				"2  3" + "\n" +
				"4  5  6"  ;
				
		System.out.println(sb);
		String[] lines = sb.split("\n", -2);
		int i = 0;
		
		int[][] numMatrix = new int[lines.length][lines.length];
		
		for (String line: lines) {
			String[] data = line.split("  ", -2);
			for (int j=0; j<data.length; j++) {
				numMatrix[i][j] = Integer.valueOf(data[j]);
			}
			i++;
		}
		return numMatrix;
	}

}
