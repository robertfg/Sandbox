package euler;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class Test2 {
	
	final static int TRADE_ID = 0;
	final static int VERSION = 1;
	final static int SECURITY_ID = 2;
	final static int QUANTITY = 3;
	//final static int TRADE = 4;
	final static int DIRECTION = 4;
	final static int ACCOUNT = 5;
	final static int OPERATION = 6;
	final static int TX = 7;
	
	final static int OUTPUT_ACCOUNT = 0;
	final static int OUTPUT_SECURITY_ID = 1;
	final static int OUTPUT_QUANTITY = 2;
	final static int OUTPUT_TRADE = 3;
	
	static String sb = "" +
			"1234|1|XYZ|100|BUY|ACC-1234|NEW" + "\n" +
			"1234|2|XYZ|150|BUY|ACC-1234|AMEND" + "\n" +
			"5678|1|QED|200|BUY|ACC-2345|NEW" + "\n" +
			"5678|2|QED|0|BUY|ACC-2345|CANCEL" + "\n" +
			"2233|1|RET|100|SELL|ACC-3456|NEW" + "\n" +
			"2233|2|RET|400|SELL|ACC-3456|AMEND" + "\n" +
			"2233|3|RET|0|SELL|ACC-3456|CANCEL" + "\n" +
			"8896|1|YUI|300|BUY|ACC-4567|NEW" + "\n" +
			"6638|1|YUI|100|SELL|ACC-4567|NEW" + "\n" +
			"6363|1|HJK|200|BUY|ACC-5678|NEW" + "\n" +
			"7666|1|HJK|200|BUY|ACC-5678|NEW" + "\n" +
			"6363|2|HJK|100|BUY|ACC-5678|AMEND" + "\n" +
			"7666|2|HJK|50|SELL|ACC-5678|AMEND" + "\n" +
			"8686|1|FVB|100|BUY|ACC-6789|NEW" + "\n" +
			"8686|2|GBN|100|BUY|ACC-6789|AMEND" + "\n" +
			"9654|1|FVB|200|BUY|ACC-6789|NEW" + "\n" +
			"1025|1|JKL|100|BUY|ACC-7789|NEW" + "\n" +
			"1036|1|JKL|100|BUY|ACC-7789|NEW" + "\n" +
			"1025|2|JKL|100|SELL|ACC-8877|AMEND" + "\n" +
			"1122|1|KLO|100|BUY|ACC-9045|NEW" + "\n" +
			"1122|2|HJK|100|SELL|ACC-9045|AMEND" + "\n" +
			"1122|3|KLO|100|SELL|ACC-9045|AMEND" + "\n" +
			"1144|1|KLO|300|BUY|ACC-9045|NEW" + "\n" +
			"1144|2|KLO|400|BUY|ACC-9045|AMEND" + "\n" +
			"1155|1|KLO|600|SELL|ACC-9045|NEW" + "\n" +
			"1155|2|KLO|0|BUY|ACC-9045|CANCEL"; 
	
	public static void main(String[] args) {
		//HashMap currentPositionMap
		//key: Account|Instrument
		//value: Quantity|Trades
		Map<String, String[]> currentPositionMap = new LinkedHashMap<String, String[]>();
		
		//HashMap transactionMap
		//key: Account|Security Identifier|Trade ID
		//value: Version|Quantity|Trade
		Map<String, String[]> transactionMap = new LinkedHashMap<String, String[]>();
		
		// parse incoming file
		// process each line and placing it on the transactionMap and currentPositionMap
		// create logic before placing in transactionMap and currentPositionMap
		String[] lines = sb.split("\n", -2);
		for (String line: lines) {
			System.out.println(line);
			String[] newTransaction = line.split("\\|", -2);
			
			String key = newTransaction[ACCOUNT] + "|" + 
					newTransaction[SECURITY_ID] + "|" + 
					//newTransaction[DIRECTION] + "|" + 
					newTransaction[TRADE_ID];
			
			
			String currentPositionKey = newTransaction[ACCOUNT] + "|" + 
					newTransaction[SECURITY_ID];
			String[] currentTransaction = null;
			if (transactionMap.containsKey(key)) {
				currentTransaction = transactionMap.get(key);
				int currentVersion = Integer.valueOf(currentTransaction[VERSION]);
				int newVersion = Integer.valueOf(newTransaction[VERSION]);
				if (newVersion > currentVersion) {
					String[] currentPosition = currentPositionMap.get(currentPositionKey);
					getCurrentPositionValue(currentPosition, newTransaction, currentTransaction);
					transactionMap.put(key, newTransaction);
				}
			}
			else {
				if (currentPositionMap.containsKey(currentPositionKey)) {
					String[] currentPosition = currentPositionMap.get(currentPositionKey);
					getCurrentPositionValue(currentPosition, newTransaction, currentTransaction);
				}
				else {
					String[] newCurrentPosition = new String[4];
					newCurrentPosition[OUTPUT_ACCOUNT] = newTransaction[ACCOUNT];
					newCurrentPosition[OUTPUT_SECURITY_ID] = newTransaction[SECURITY_ID];
					getCurrentPositionValue(newCurrentPosition, newTransaction, null);
					currentPositionMap.put(currentPositionKey, newCurrentPosition);
				}
				transactionMap.put(key, newTransaction);
				
			}
		}
		System.out.println("Total Transactions: " + lines.length);

		System.out.println("");
		System.out.println("");
		System.out.println("Final Output: ");
		for (Entry<String, String[]> entry: currentPositionMap.entrySet()) {
			String[] finalPosition = entry.getValue();
			System.out.println(finalPosition[OUTPUT_ACCOUNT] + "|" + 
					finalPosition[OUTPUT_SECURITY_ID] + "|" +
					finalPosition[OUTPUT_QUANTITY] + "|" +
					finalPosition[OUTPUT_TRADE]);
		}
	}

	private static int getCurrentPositionValue(String[] currentPosition, String[] newTransaction, String[] pastTransaction) {
		int currentPositionValue = 0;
		if (currentPosition[OUTPUT_QUANTITY] != null) {
			currentPositionValue = Integer.valueOf(currentPosition[OUTPUT_QUANTITY]);
		}
		int pastTransactionValue = 0;
		if (pastTransaction != null) {
			pastTransactionValue = Integer.valueOf(pastTransaction[QUANTITY]);
			if (!isIncrement(pastTransaction[DIRECTION])) {
				pastTransactionValue = pastTransactionValue * -1;
			}
		}
		int newTransactionValue = Integer.valueOf(newTransaction[QUANTITY]);
		if (!isIncrement(newTransaction[DIRECTION])) {
			newTransactionValue = newTransactionValue * -1;
		}
		currentPositionValue = currentPositionValue - pastTransactionValue + newTransactionValue;
		System.out.println("currentPositionValue: " + currentPositionValue);
		
		currentPosition[OUTPUT_QUANTITY] = "" + currentPositionValue;
		if (currentPosition[OUTPUT_TRADE] != null) {
			currentPosition[OUTPUT_TRADE] = removeDuplicate(currentPosition[OUTPUT_TRADE], newTransaction[TRADE_ID]);
		}
		else {
			currentPosition[OUTPUT_TRADE] = newTransaction[TRADE_ID];
		}
		
		return currentPositionValue;
	}
	
	private static boolean isIncrement(String direction) {
		if (!"SELL".equals(direction)) {
			return true;
		}
		
		return false;
	}
	
	private static String removeDuplicate(String orig, String addition) {
		orig = orig + "," + addition;
		String[] arr = orig.split(",", -2);
		Set<String> unique = new LinkedHashSet<String>();
		unique.addAll(Arrays.asList(arr));
		orig = "";
		for (String str: unique) {
			orig = orig + str + ",";
		}
		orig = orig.substring(0, orig.length()-1);
		
		return orig;
	}
}
