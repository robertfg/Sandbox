package generics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericMethods {

	public static void main(String[] args) {
		
		GenericMethods gen = new GenericMethods();
		
		List<Map<String,String>> ret =   GenericMethods.select();
		 
		
          System.out.println( ret.get(0).get("key") );
	}

	
	public static <T> List<Map<String,T>> select(){
		
		
		List<Map<String,T>> ret = new ArrayList<Map<String,T>>();
		Map<String,T> x = new HashMap<String,T>();
		x.put("key", (T) "String" );
		ret.add(x);
		
		return ret;
	}
}
