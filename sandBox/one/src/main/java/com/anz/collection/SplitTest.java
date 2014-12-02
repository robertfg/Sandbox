package com.anz.collection;

import java.util.HashMap;
import java.util.Map;

public class SplitTest {

	
	public static void main(String[] args) {
	
		String period = "christopher#anabo";
		String split[] = period.split("\\#");
		System.out.println(  split[0] );
		
		System.out.println( period.split("\\.").length );
	}
	
}
