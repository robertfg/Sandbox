package com.anz.rer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class AppLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double x = 6.7;
		
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:resources/SPRING-INF/applicationContext.xml");
      // AppLauncher.testMap(); 
		
	}
	
	public static void  testMap(){
		
		ConcurrentMap<String, Integer> varDates=new ConcurrentHashMap<String,Integer>();
		
		Map<String,Integer> a = new ConcurrentHashMap<String, Integer>();
	                  a .put("a", 1);
	                  
	                  System.out.println( a );
	                  
	                  a.put("a", 2);
	                  System.out.println( a );
	                  
	                  
	                  varDates.putIfAbsent("A", 1);
	                  System.out.println(varDates);
	                  
	                  varDates.putIfAbsent("A", 2);
	                  System.out.println(varDates);
	                  
	                  
	                  double x = +200;
	                  double w = -200;
	                  
	                  
	}

} 
