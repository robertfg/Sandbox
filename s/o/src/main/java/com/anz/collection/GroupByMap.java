package com.anz.collection;

import java.util.HashMap;
import java.util.Map;

public class GroupByMap extends HashMap<String, Integer> {

	private static final long serialVersionUID = 1L;

	
	@Override
	public Integer put(String key, Integer value) {
		if(super.get(key)!=null){
			value += super.get(key).intValue();
		} 
		return super.put(key, value);
	}

	public static void main(String[] args){
		GroupByMap g = new GroupByMap();
				   g.put("apple", 100);
				   g.put("apple", 100);
				   g.put("apple", 100);
				   g.put("apple", 100);
				   g.put("apple", 100);
	
				   g.put("guava", 100);
				   g.put("guava", 100);
				   g.put("guava", 100);
				   g.put("guava", 100);
				   g.put("guava", 100);
				   
				   g.put("banana", 100);
				   g.put("banana", 100);
				   
				   g.put("grapes", 100);
				   g.put("banana", 100);
				   
				   g.put("kiwi", 100);
				   
				   g.put("strawberry", 100);
				   
		for(Map.Entry<String,Integer> groupBy: g.entrySet()){
			System.out.println(groupBy.getKey() + ":" + groupBy.getValue());
		}
		
		
		
		
	}
	
}
