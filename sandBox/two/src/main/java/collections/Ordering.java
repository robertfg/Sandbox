package collections;

import java.util.LinkedHashSet;

public class Ordering {

	public static void main(String[] args) {
		LinkedHashSet<String> lSet = new LinkedHashSet<>();
		
		lSet.add("E");
		lSet.add("A");
		lSet.add("B");
		lSet.add("C");
		lSet.add("D");
		
		
		for (String string : lSet) {
			System.out.println( string );
		}
		

	}

}
