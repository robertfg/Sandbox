package lamda.custom;

import java.util.Arrays;

public class StaticMethod {

	public static int myCompare(String in, String out) {
		return in.length() - out.length();
	}

	public static void main(String[] args) {
		Arrays.sort(args, StaticMethod::myCompare);
	}
}