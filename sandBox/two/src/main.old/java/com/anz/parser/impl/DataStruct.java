package com.anz.parser.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStruct {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		   
		
		           Map<String, Map<String, List<String>>> dataStruct = new HashMap<String, Map<String,List<String>>>();
		         
		           Map<String,List<String>> records = new HashMap<String, List<String>>();
		            
		           
		           //while
		            if("A" == null){ 
		            	 List<String> record =new ArrayList<String>();
		                 records.put("A", record );
		            } else {
		                 records.get("A").add("rec2");
		              
		            }
		           //end
		            dataStruct.put("Container1", records);
		           
                    List<String> mappers = new ArrayList<String>();
                                 
                    for (String string : mappers) {
					     				
					}
                     
		            
		            /*
		             * 
		             *  
		             *  
		             *  file1 -> mapper[] -> read each mapper[0] -> put in data struct 
		             *            
		             *      
		             *      
		             *      
		             *  
		            */
		
	}

}
