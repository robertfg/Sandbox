package memory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringTokenizer;

public class BustTokenizer {
	private static HashMap<String, String> tunnelMap = new HashMap<String, String>();

	/*
	 * private static BustTokenizer instance = new BustTokenizer();
	 * 
	 * public static BustTokenizer getInstance(){ return instance; }
	 */
	public void test(String[] args) throws Exception {
		long i = 0;

		System.out.println("started");
		BufferedReader reader = new BufferedReader(new FileReader(
				"c:/test/test.txt"));

		int c = reader.read();
		StringBuffer sb = new StringBuffer();
		while (c != -1) {
			sb.append((char) c);
			c = reader.read();
		}
		reader.close();
		System.out.println("completed reading file");
		String data = sb.toString();

		StringTokenizer stLines = new StringTokenizer(data, "\n");
		while (stLines.hasMoreTokens()) {
			String token = stLines.nextToken();
			StringTokenizer stFields = new StringTokenizer(token, ",");
			while (stFields.hasMoreTokens()) {
				String fieldToken = stFields.nextToken();
				tunnelMap.put(fieldToken, fieldToken);
			}
		}
		System.out.println("completed");
	}

	public static void main(String[] args) throws Exception {
		new BustTokenizer().test(null);

	}
}