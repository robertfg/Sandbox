package com.anz.collection;

import java.util.HashMap;
import java.util.Map;

public class MapTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String,String> map = new HashMap<String,String>();
     
		 map.put("a", "1");
		 map.put("a", "2");
		 
		 
		 
		 System.out.println( map.get("a"));
	   
	}

}
