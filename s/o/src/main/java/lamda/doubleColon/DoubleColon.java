package lamda.doubleColon;

import java.util.*;
import java.util.stream.*;

public class DoubleColon {

	public static void display(String name) {
		System.out.println("Name : " + name);
	}

	public static void main(String[] args) {
		List<String> names = Arrays.asList("one", "two", "three");
		names.forEach(DoubleColon::display);
	}
}