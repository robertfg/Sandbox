package collections;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class Ordering {

	public static void main(String[] args) {
		LinkedHashSet<String> lSet = new LinkedHashSet<String>();
		
		lSet.add("B");
		lSet.add("E");
		lSet.add("A");
	
		lSet.add("C");
		lSet.add("D");
		
		
		for (String string : lSet) {
			System.out.println( string );
		}
		

		Map<String,Integer> fruits = new HashMap<String,Integer>();
		
		fruits.put("apple", 1);
		fruits.put("apple", 1);
		
		System.out.println( fruits.get("apple").intValue() );
		
	}
	
	
	

}
