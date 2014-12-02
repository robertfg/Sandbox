package com.eyeota.codingfun.cache;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LookUpUtil {
  
	public void loadData(String json, OffHeapStr strPool ,
									  OffHeapInt orgIndex,
									  OffHeapInt paramNameIndex,
									  OffHeapInt paramValueIndex,
									  OffHeapInt segmentValueIndex){
		
		ObjectMapper mapper = new ObjectMapper();
	
		try {
			List<Map<String,List<Map<String,List< Map<String,Map<String,String>>>>>>> cache = 
					mapper.readValue(new File(json),
							new TypeReference<List<Map<String,List<Map<String,List< Map<String,Map<String,String>>>>>>>>() { });
			
			for (Map<String, List<Map<String, List<Map<String, Map<String, String>>>>>> map : cache) {
				for (Map.Entry<String, List<Map<String, List<Map<String, Map<String, String>>>>>> map2: map.entrySet()) {
					
				//	System.out.println("Org:" + map2.getKey());
					int orgCode = addIndex(map2.getKey() , orgIndex, strPool);
					    System.out.println("orgCode:" + orgCode);
				
					for (Map<String, List<Map<String, Map<String, String>>>> map3 : map2.getValue()) {
						for ( Entry<String, List<Map<String, Map<String, String>>>> map4 : map3.entrySet()) {
							
							//System.out.println("ParamName:" + map4.getKey());
						    
							int paramNameCode =  addJoin(orgCode,  map4.getKey(), orgCode, paramNameIndex, strPool);
						        paramNameCode = strPool.getIndex( map4.getKey().getBytes());
						       System.out.println("paramNameCode:" + paramNameCode); 		
						    
						    for (Map<String, Map<String, String>> map5 : map4.getValue()) {
						    	for (Entry<String, Map<String, String>> map6 : map5.entrySet()) {
									String paramValKeys = map6.getKey();
								//	System.out.println("ParamVal:" + paramValKeys);
									
									if(paramValKeys.contains("\n")){
						    			String[] paramValKey = paramValKeys.split("\n");
						    			for (int i = 0; i < paramValKey.length; i++) {
						    				
						    				 int paramValCode =  addJoin(paramNameCode,  paramValKey[i], paramNameCode, paramValueIndex, strPool);
						    			   	     paramValCode = strPool.getIndex( paramValKey[i].getBytes());
						    				 
								    		 Map<String, String> map7 = map6.getValue();
										     for (Entry<String,String> segment : map7.entrySet()) {
										    	 	int segmentValue =  addJoin(paramValCode,  segment.getValue(),  paramNameCode, segmentValueIndex, strPool);
											 }
						    			}
						    		} else {
						    			 System.out.println("load data K:" + paramNameCode + ",V:" + map6.getKey());
						        	   	 
						    			 int paramValCode =  addJoin(paramNameCode,  map6.getKey(), orgCode, paramValueIndex, strPool);
						        	   	     paramValCode = strPool.getIndex(map6.getKey().getBytes());
							    	 	 
						        	   	     Map<String, String> map7 = map6.getValue();
									     
							    	 	   for (Entry<String,String> segment : map7.entrySet()) {
									    	   System.out.println("load data K:" + paramValCode + ",V:" + segment.getValue());
									    	   System.out.println("load data K:" + new String( strPool.getValue(paramValCode) )
									    	   + ",V:" + segment.getValue());
									    	   
									    	 	int segmentValue =  addJoin(paramValCode,  segment.getValue(),paramNameCode, segmentValueIndex, strPool);
										 }
						    		}
							   }
							}
						}
					} 
				}
			}
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Link twoo Index provider
	 * @param idxValue
	 * @param intIndexPool
	 * @param strPool
	 * @return
	 */
	public int addIndex( String idxValue, OffHeapInt intIndexPool, OffHeapStr strPool ){
		 int intIndexPoolidxValueCode = -1; 
		 int idxStrPoolCode = strPool.getIndex(idxValue.getBytes());
		 
		 if(idxStrPoolCode <=-1){ // idxValue doesn't  exist in StringPool
			 idxStrPoolCode = strPool.create(idxValue.getBytes());
		 } 
		 
		 if( intIndexPool.exist(idxStrPoolCode) ) { // check if idxStrPoolCode exist in intIndexPool
			 /* already exist */
			 intIndexPoolidxValueCode = intIndexPool.getIndex(idxStrPoolCode);
		 } else {
			 /* if dont exist then creat*/
			 intIndexPoolidxValueCode = intIndexPool.create(idxStrPoolCode,idxStrPoolCode,-1);
		 }
		 return intIndexPoolidxValueCode;
	}
	
	public int addJoin( int idxCode, String idxValue, int parent, OffHeapInt intIndexPool, OffHeapStr strPool ){
		 int intIndexPoolidxValueCode = -1; 
		 int idxStrPoolCode = strPool.getIndex(idxValue.getBytes());
	
		 if(idxStrPoolCode <=-1){ // idxValue doesn't  exist in StringPool
			 idxStrPoolCode = strPool.create(idxValue.getBytes());
		 } 
		 System.out.println(idxValue + "=" + idxStrPoolCode);
		 if( intIndexPool.exist(idxStrPoolCode) ) { // check if idxStrPoolCode exist in intIndexPool
			 
			 int[] kS =   intIndexPool.getKs(idxStrPoolCode); // return 0,1,2
			 boolean exist = false;
			 for (int i = 0; i < kS.length; i++) {
				int j = kS[i];
				if(idxCode == j){
					exist = true;
				}
			}
			 
			 if(!exist){
				 intIndexPoolidxValueCode = intIndexPool.create(idxCode,idxStrPoolCode,parent);
				 intIndexPoolidxValueCode = idxStrPoolCode;
			 } else {
				 /* already exist */
				 // TODO must create method to get index using key and value
				// intIndexPoolidxValueCode = intIndexPool.getIndex(idxStrPoolCode);
				 return idxStrPoolCode;
			 }
			 
			 
			 
		 } else {
			 /* if dont exist then creat*/
			 intIndexPoolidxValueCode = intIndexPool.create(idxCode,idxStrPoolCode,parent);
			 intIndexPoolidxValueCode = idxStrPoolCode;
		 }
		 return intIndexPoolidxValueCode;
	}
	
	
	
	/**
	 * Search the IndexProvider for specific code
	 * @param intIndexPool Index Provider
	 * @param strPoolCode the code from the StringProvider
	 * @return boolean if exist
	 */
	public boolean containsKey(OffHeapInt intIndexPool, int key){
        return intIndexPool.containsKey(key);
    }

	public boolean containsKeyValue(OffHeapInt intIndexPool, int key,int value){
        return intIndexPool.containsKeyValue(key, value);
    }
	
	
	/**
	 * Search the IndexProvider using default search if specific code was not found
	 * @param intIndexPool Index Provider
	 * @param strPoolCode the code from the StringProvider
	 * @return boolean if exist
	 */
	public boolean searchDefault(OffHeapInt intIndexPool, int strPoolCode, int strDefaultCode) {
        if( !intIndexPool.exist(strDefaultCode)){
        	return intIndexPool.exist(strPoolCode);
        } else {
        	return true;
        }
    }
	
	
	/**
	 * Search the StringPool using string value
	 * @param strPool StringPool Provider
	 * @param value string keyword used to search in the StringPool
	 * @return int code of specific String in the Pool
	 */
	public int getStringPoolCode(OffHeapStr strPool, String value){
		return strPool.getIndex(value.getBytes());
	}
	
	/**
	 * @param strPool
	 * @param value
	 * @return int code of specific String in the Pool
	 */
	public int addTotStringPoolCode(OffHeapStr strPool, String value){
			return strPool.create(value.getBytes());
	}
	
	
	
}
