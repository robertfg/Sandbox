package com.eyeota.codingfun.cache;

import java.util.HashSet;
import java.util.Set;

public class LookupCacheProvider implements LookupCache {
  
	private OffHeapStr strPool =   			new LookUpStr(3000);
	private OffHeapInt orgIndex =   		new Index(500);
	private OffHeapInt paramNameIndex =   	new Index(500);
	private OffHeapInt paramValueIndex =  	new Index(500);
	private OffHeapInt segmentValueIndex =  new Index(500);
	private LookUpUtil lookUpUtil = new LookUpUtil();
	private int STR_BLANK_CODE = -1;
	private static String BLANK = "";
	
	@Override
	public SegmentConfig[] getSegmentFor(String orgKey, String paramKey) {
		
		int orgKeyCode   = lookUpUtil.getStringPoolCode(strPool, orgKey);
		int paramKeyCode = lookUpUtil.getStringPoolCode(strPool, paramKey);
		STR_BLANK_CODE  = lookUpUtil.getStringPoolCode(strPool, BLANK);
		Set<String> segmentConfigs = new HashSet<String>();
		
		
		if( lookUpUtil.containsKey( orgIndex, orgKeyCode)){ // search in org
			System.out.println(orgKey + ":exist");
		
			if( lookUpUtil.containsKeyValue(paramNameIndex, orgKeyCode, paramKeyCode  )){ // search in paramname
			    System.out.println(paramKey + ":exist");
				  
				  if (lookUpUtil.containsKeyValue(paramValueIndex, paramKeyCode, STR_BLANK_CODE  )) { //search in paramValue
					  
					  int[] pValueIndex  = paramValueIndex.getIndex(paramKeyCode, STR_BLANK_CODE) ;
					  
					  		  for (int i = 0; i < pValueIndex.length; i++) {
							    	  System.out.println(  "S:" + new String(strPool.getValue( paramValueIndex.getValue(pValueIndex[i]) )) );
							    	
							    	  System.out.println(  "Sx:" +  paramValueIndex.getValue(pValueIndex[i]) );
								    	
							    	/*  int[] segValueIndex = segmentValueIndex.getIndexK(  paramValueIndex.getValue(pValueIndex[i]) );
					    			*/   
							    	  System.out.println("Value:" + paramValueIndex.getValue(pValueIndex[i]) + ",Key:" +  paramValueIndex.getKey(pValueIndex[i]));
							    	 
							    	  int[] segValueIndex = segmentValueIndex.getIndex( paramValueIndex.getValue(pValueIndex[i]), 
							    			                        -1, paramValueIndex.getKey(pValueIndex[i]));
					    			  
							    	  
							    	  for (int j = 0; j < segValueIndex.length; j++) {
					    				   //segmentConfigs.add(new SegmentConfig(  new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] ))) ) );
							    		  
							    		  System.out.println( "key:"   +  new String(strPool.getValue( segmentValueIndex.getKey( segValueIndex[j] )))  );
							    		  System.out.println( "value:" +  new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] ))) );
							    		  
					    				   segmentConfigs.add(  new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] ))) ) ;
					    			   }
							  }	  
				  } /*else {
					  	int[] pValueIndex  = paramValueIndex.getIndexK(paramKeyCode);
					  	for (int i = 0; i < pValueIndex.length; i++) {
					  	//	System.out.println(  new String(strPool.getValue( paramValueIndex.getValue(pValueIndex[i])))  );
					    	   int[] segValueIndex = segmentValueIndex.getIndexK(paramValueIndex.getValue(pValueIndex[i]));
					    			   for (int j = 0; j < segValueIndex.length; j++) {
					    				   //segmentConfigs.add(new SegmentConfig( new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] ))) ) );
					    				   segmentConfigs.add(  new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] ))) ) ;
					    				   
									}
						}	  
				  }*/
				  
				   				
			}	
		}
		
		SegmentConfig[] result = new SegmentConfig[segmentConfigs.size()];
		int ctr =0;
		for (String segmentConfig : segmentConfigs) {
			result[ctr ] = new SegmentConfig(segmentConfig);
			ctr++;
		}
		
		
		return result;
	}
	
	

	@Override
	public SegmentConfig[] getSegmentFor(String orgKey, String paramKey, String paramValKey) {
	
		int orgKeyCode   = lookUpUtil.getStringPoolCode(strPool, orgKey);
		int paramKeyCode = lookUpUtil.getStringPoolCode(strPool, paramKey);
		int paramValKeyCode = lookUpUtil.getStringPoolCode(strPool, paramValKey);
		STR_BLANK_CODE  = lookUpUtil.getStringPoolCode(strPool, BLANK);
		Set<String> segmentConfigs = new HashSet<String>();
		
		if( lookUpUtil.containsKey( orgIndex, orgKeyCode)){ // search in org
			//System.out.println(orgKey + ":exist");
		
			if( lookUpUtil.containsKeyValue(paramNameIndex, orgKeyCode, paramKeyCode  )){ // search in paramname
				  // System.out.println(paramKey + ":exist");
				  
				  if (lookUpUtil.containsKeyValue(paramValueIndex, paramKeyCode, STR_BLANK_CODE  ) && paramValKey == "") { //search in paramValue
					  int[] pValueIndex  = paramValueIndex.getIndex(paramKeyCode, STR_BLANK_CODE) ;
					  		  for (int i = 0; i < pValueIndex.length; i++) {
							    //	 System.out.println( new String(strPool.getValue( paramValueIndex.getValue(pValueIndex[i]) )) );
							    //	 int[] segValueIndex = segmentValueIndex.getIndexK(paramValueIndex.getValue(pValueIndex[i]));
							    	 
							    	 int[] segValueIndex = segmentValueIndex.getIndex( paramValueIndex.getValue(pValueIndex[i]), 
		    			                        -1, paramValueIndex.getKey(pValueIndex[i]));
							    	 
							    	 
					    			   for (int j = 0; j < segValueIndex.length; j++) {
										
					    				   //segmentConfigs.add(new SegmentConfig(new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] ))) ));
					    				   segmentConfigs.add( new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] ))) );
					    				   
					    			   }
					  		  }
					  		  
					  		int[] pValueNonBlankIndex  =  paramValueIndex.getIndex(paramKeyCode, paramValKeyCode);
						  	for (int i = 0; i < pValueNonBlankIndex.length; i++) {
						  		  // System.out.println(  new String(strPool.getValue( paramValueIndex.getValue(pValueNonBlankIndex[i])))  );
						    	 //  int[] segValueIndex = segmentValueIndex.getIndexK(paramValueIndex.getValue(pValueNonBlankIndex[i]));
						    	   
						    	   int[] segValueIndex = segmentValueIndex.getIndex( paramValueIndex.getValue(pValueNonBlankIndex[i]), 
	    			                        -1, paramValueIndex.getKey(pValueNonBlankIndex[i]));
						    	   
						    			   for (int j = 0; j < segValueIndex.length; j++) {
						    				   //segmentConfigs.add(new SegmentConfig(  new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] ))))) ;
						    				   segmentConfigs.add(  new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] )))) ;
										}
							}	  
				  } else {
					  
						int[] pValueIndex  =  paramValueIndex.getIndex(paramKeyCode, paramValKeyCode);
					  
					  
					  	for (int i = 0; i < pValueIndex.length; i++) {
					  		  // System.out.println(  new String(strPool.getValue( paramValueIndex.getValue(pValueIndex[i])))  );
					    	   int[] segValueIndex = segmentValueIndex.getIndexK(paramValueIndex.getValue(pValueIndex[i]));
					    			   for (int j = 0; j < segValueIndex.length; j++) {
					    				   //segmentConfigs.add(new SegmentConfig( new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] ))))) ;
					    				   
					    				   segmentConfigs.add( new String(strPool.getValue( segmentValueIndex.getValue( segValueIndex[j] )))) ;
									}
						}	  
				  }
				  
				   				
			}	
		}
		SegmentConfig[] result = new SegmentConfig[segmentConfigs.size()];
		int ctr =0;
		for (String segmentConfig : segmentConfigs) {
			result[ctr ] = new SegmentConfig(segmentConfig);
			ctr++;
		}
		
		
		return result;
	}
	
	public void loadData(String json){
		lookUpUtil.loadData(json, strPool, orgIndex, paramNameIndex, paramValueIndex, segmentValueIndex);
	}
	
	public void test(String json){
			loadData(json);
		
			SegmentConfig[] segment = getSegmentFor("org1","paramName1");
	  		
			for (int i = 0; i < segment.length; i++) {
				System.out.println( "x:" +segment[i].getSegmentId() );
	  		}
		
	}
	
	
	
	

}
