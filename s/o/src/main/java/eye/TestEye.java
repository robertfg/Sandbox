package eye;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestEye {

	private final int ONE_MILLION = 1000000;
	
	@Test
	public void loadJsonAndPutToMemory(){

		OffHeapStr strPool =   			new LookUpStr(30);
		OffHeapInt orgIndex =   		new Index(50);
		OffHeapInt paramNameIndex =   	new Index(50);
		OffHeapInt paramValueIndex =  	new Index(50);
		OffHeapInt segmentValueIndex =  new Index(50);
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			List<Map<String,List<Map<String,List< Map<String,Map<String,String>>>>>>> cache = 
				/*	mapper.readValue(new File("c:\\devs\\sample.json"),*/
					mapper.readValue(new File("/home/degs/devs/space/dilzio-eyeota-coding-project-c400cb339a60/src/test/resources/data2.json"),
							new TypeReference<List<Map<String,List<Map<String,List< Map<String,Map<String,String>>>>>>>>() { });
			
			for (Map<String, List<Map<String, List<Map<String, Map<String, String>>>>>> map : cache) {
				for (Map.Entry<String, List<Map<String, List<Map<String, Map<String, String>>>>>> map2: map.entrySet()) {
					int orgCode = addIndex(map2.getKey() , orgIndex, strPool);
				
					for (Map<String, List<Map<String, Map<String, String>>>> map3 : map2.getValue()) {
						for ( Entry<String, List<Map<String, Map<String, String>>>> map4 : map3.entrySet()) {
						    int paramNameCode =  addJoin(orgCode,  map4.getKey(), paramNameIndex, strPool);
	
						    for (Map<String, Map<String, String>> map5 : map4.getValue()) {
						    	for (Entry<String, Map<String, String>> map6 : map5.entrySet()) {
							    
						     	 
						    		String paramValKeys = map6.getKey();
						    		if(paramValKeys.contains("\n")){
						    			String[] paramValKey = paramValKeys.split("\n");
						    			for (int i = 0; i < paramValKey.length; i++) {
						    				 int paramValCode =  addJoin(paramNameCode,  paramValKey[i], paramValueIndex, strPool);
								    		 Map<String, String> map7 = map6.getValue();
										     for (Entry<String,String> segment : map7.entrySet()) {
										    	 	int segmentValue =  addJoin(paramValCode,  segment.getValue(), segmentValueIndex, strPool);
											 }
						    			}
						    			
						    		} else {
						    			
						    		
							    	   	 int paramValCode =  addJoin(paramNameCode,  map6.getKey(), paramValueIndex, strPool);
							    	 	 Map<String, String> map7 = map6.getValue();
									     for (Entry<String,String> segment : map7.entrySet()) {
									    	 	int segmentValue =  addJoin(paramValCode,  segment.getValue(), segmentValueIndex, strPool);
										 }
						    		}
							   }
							}
						}
					} 
				}
			}
			System.out.println( new String(strPool.getValue(0)));
			System.out.println( new String(strPool.getValue(1)));
			System.out.println( new String(strPool.getValue(2)));
			System.out.println( new String(strPool.getValue(3)));
			System.out.println( new String(strPool.getValue(4)));
			System.out.println( new String(strPool.getValue(5)));
			System.out.println( new String(strPool.getValue(6)));
			System.out.println( new String(strPool.getValue(7)));
			System.out.println( new String(strPool.getValue(8)));
			
			System.out.println( new String(strPool.getValue(9)));
			System.out.println( new String(strPool.getValue(10)));
			System.out.println( new String(strPool.getValue(11)));
			System.out.println( new String(strPool.getValue(12)));
			System.out.println( new String(strPool.getValue(13)));
			System.out.println( new String(strPool.getValue(14)));
			System.out.println( "index of blank:" + strPool.getIndex("".getBytes()  ));	
			
			
			int values[] = paramNameIndex.getKs(0);
			for (int i = 0; i < values.length; i++) {
				System.out.println( values[i]  ) ;
			}
			
			
		} catch (JsonParseException e) {
			
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	@Test
	public void testAddStrPoolObject( ){

		OffHeapStr strPool =   new LookUpStr(6);
		
		
		Assert.assertEquals( 0, addStrPool( "".getBytes(), strPool));
		System.out.println( new String( strPool.getValue(0) ) );
		Assert.assertEquals( 0, addStrPool( "".getBytes(), strPool));
		
		Assert.assertEquals( 1, addStrPool( "org0".getBytes(), strPool));
		Assert.assertEquals( 2, addStrPool( "org1".getBytes(), strPool));
		Assert.assertEquals( 3, addStrPool( "org3".getBytes(), strPool));
	}
	
	@Test
	public void testIndexGetValue() {
		OffHeapInt orgIndex = new Index(3);
		Assert.assertEquals(-1, orgIndex.getIndex(0) );
	}
	
	@Test
	public void testBlankKey(){
		OffHeapStr strPool =   new LookUpStr(6);
		Assert.assertEquals( 0, addStrPool( "".getBytes(), strPool));
		System.out.println( new String( strPool.getValue(0) ) );
	}
	
	@Test
	public void testAddIndexObject( ){
		OffHeapInt orgIndex =   new Index(5);
		Assert.assertEquals( 0, addIndex( 0,0, orgIndex));
		Assert.assertEquals( 0, addIndex( 0,0, orgIndex));
		Assert.assertEquals( 1, addIndex( 1,1, orgIndex));
		Assert.assertEquals( 1, addIndex( 1,1, orgIndex));
		Assert.assertEquals( 1, addIndex( 1,1, orgIndex));
		Assert.assertEquals( addIndex( 2,2,  orgIndex),2);
	    Assert.assertEquals( addIndex( 3,3, orgIndex),3);
	    Assert.assertEquals( addIndex( 4,4, orgIndex),4);
	}
	
	@Test
	public void testAddIndexAndAddStringPool( ){
		OffHeapInt orgIndex =  orgIndex = new Index(3);
		OffHeapStr stringPoolLookUp = stringPoolLookUp = new LookUpStr(3); 
		String indexValue = "org1";
	    addIndex(indexValue, orgIndex,stringPoolLookUp);
	    Assert.assertEquals( stringPoolLookUp.getIndex( indexValue.getBytes()) , orgIndex.getValue(stringPoolLookUp.getIndex( indexValue.getBytes())));
	    Assert.assertEquals(indexValue, stringPoolLookUp.getIndex( indexValue.getBytes()) , orgIndex.getValue(stringPoolLookUp.getIndex( indexValue.getBytes())));

	    
	    addIndex(indexValue, orgIndex,stringPoolLookUp);
	    Assert.assertEquals( stringPoolLookUp.getIndex( indexValue.getBytes()) , orgIndex.getValue(stringPoolLookUp.getIndex( indexValue.getBytes())));
	    Assert.assertEquals(indexValue, stringPoolLookUp.getIndex( indexValue.getBytes()) , orgIndex.getValue(stringPoolLookUp.getIndex( indexValue.getBytes())));

	    indexValue = "org2";
	    addIndex(indexValue, orgIndex,stringPoolLookUp);
	    Assert.assertEquals( stringPoolLookUp.getIndex( indexValue.getBytes()) , orgIndex.getValue(stringPoolLookUp.getIndex( indexValue.getBytes())));
	    Assert.assertEquals(indexValue, stringPoolLookUp.getIndex( indexValue.getBytes()) , orgIndex.getValue(stringPoolLookUp.getIndex( indexValue.getBytes())));
  
	    indexValue = "org3";
	    addIndex(indexValue, orgIndex,stringPoolLookUp);
	    Assert.assertEquals( stringPoolLookUp.getIndex( indexValue.getBytes()) , orgIndex.getValue(stringPoolLookUp.getIndex( indexValue.getBytes())));
	    Assert.assertEquals(indexValue, stringPoolLookUp.getIndex( indexValue.getBytes()) , orgIndex.getValue(stringPoolLookUp.getIndex( indexValue.getBytes())));

	}
	
	@Test
	public void testObjectJoin(){
		OffHeapStr strPool =   			new LookUpStr(30);
		OffHeapInt orgIndex =   		new Index(5);
		OffHeapInt paramNameIndex =   	new Index(5);
		OffHeapInt paramValueIndex =  	new Index(5);
		OffHeapInt segmentValueIndex =  new Index(5);
		 
		
	  	 int orgCode        =  strPool.create( "org1".getBytes() );
	  	 int paramNameCode  =  strPool.create( "paramName1".getBytes() );
	  	 int testEduCode    =  strPool.create( "testedu".getBytes() );
		 
	  	 addIndex(orgCode,orgCode,orgIndex);
	  	 addJoin(orgCode, paramNameCode, paramNameIndex);
	   	 addJoin(orgCode, testEduCode, paramNameIndex);
	  	 
	  	 System.out.println( paramNameIndex.exist( orgCode ) );
	//   System.out.println( paramNameIndex.exist( testEduCode ) );
	//   	 LookUpUtil util = new LookUpUtil();
		
	}
	
	
	@Test
	public void testGetVs(){
		 OffHeapInt offHeap = new Index(5);
					offHeap.create(0, 99);
					offHeap.create(0, 1);
					offHeap.create(0, 3);

		int[] vS = offHeap.getVs(0);
		
		System.out.println( vS.toString() );
	    Assert.assertArrayEquals(new int[]{99,1,3}, offHeap.getVs(0) );	

 	     OffHeapInt paramName = new Index(5);
	    			paramName.create(0, 99);
				    paramName.create(1, 99);
				    paramName.create(2, 99);

		int[] kS = paramName.getKs(99);
					System.out.println( kS.toString());
					  Assert.assertArrayEquals(new int[]{0,1,2}, paramName.getKs(99) );	
	}
	
	@Test
	public void testContains(){
		OffHeapInt offHeap = new Index(5);
		offHeap.create(0, 99);
		offHeap.create(1, 1);
		offHeap.create(2, 3);
		
	Assert.assertEquals(true, offHeap.containsKey(0));	
	Assert.assertEquals(true, offHeap.containsKey(2));	
	Assert.assertEquals(true, offHeap.containsValue(99));
	Assert.assertEquals(true, offHeap.containsValue(3));
	
	Assert.assertEquals(true, offHeap.containsKeyValue(0,99));
	Assert.assertEquals(true, offHeap.containsKeyValue(1,1));
	Assert.assertEquals(true, offHeap.containsKeyValue(2,3));
	
		
	
	}

	
	
	@Test
	public void testGet(){
		OffHeapInt offHeap = new Index(5);
		offHeap.create(0, 99);
		offHeap.create(1, 1);
		offHeap.create(2, 3);
		
		offHeap.containsKey(0);
		offHeap.containsValue(99);
	
		offHeap.jump(0);
		Assert.assertEquals(99, offHeap.getValue());
		Assert.assertEquals(0, offHeap.getIndex());
		
		offHeap.jump(2);
		Assert.assertEquals(2, offHeap.getKey());
		
		int[] index =  offHeap.getIndex(0,99);
		Assert.assertArrayEquals( new int[]{0}, index);
		
	    index =  offHeap.getIndexV(3);
		Assert.assertArrayEquals( new int[]{2}, index);
	 
		index =  offHeap.getIndexK(2);
		Assert.assertArrayEquals( new int[]{2}, index);
		
		
		
		
	}
	
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
			 intIndexPoolidxValueCode = intIndexPool.create(idxStrPoolCode,idxStrPoolCode);
		 }
		 return intIndexPoolidxValueCode;
	}
	
	
	public int addIndex( int idxCode, int valueCode, OffHeapInt intIndexPool ){
		 int intIndexPoolidxValueCode = -1; 
		 
		 if( intIndexPool.exist(idxCode) ) { // check if idxStrPoolCode exist in intIndexPool
			 /* already exist */
			 intIndexPoolidxValueCode = intIndexPool.getIndex(idxCode);
		 } else {
			 /* if dont exist then creat*/
			 intIndexPoolidxValueCode = intIndexPool.create(valueCode, idxCode);
		 }
		 return intIndexPoolidxValueCode;
	}

	@Test
	public void testAddJoin(){
		 OffHeapInt org = new Index(5);
		 org.create(0, 99);
		 org.create(1, 1);
		 org.create(2, 3);
		
		 OffHeapInt paramName = new Index(5);
		
		 addJoin(0,100,paramName);
		 addJoin(1,100,paramName);
		 addJoin(2,100,paramName);
		  
		 int[] kS = paramName.getKs(100);
		 for (int i = 0; i < kS.length; i++) {
			 System.out.println(kS[i]);
		}
		
					
	}
	
	public int addJoin( int idxCode, int valueCode, OffHeapInt intIndexPool ){
		 int intIndexPoolidxValueCode = -1; 
		 
		 if( intIndexPool.exist(valueCode) ) { // check if idxStrPoolCode exist in intIndexPool
			 /* check if the key is equal to idxCode */
			 
			 int[] kS =   intIndexPool.getKs(valueCode);
			 boolean exist = false;
			 for (int i = 0; i < kS.length; i++) {
				int j = kS[i];
				if(idxCode == j){
					exist = true;
				}
			}
			 
			 if(!exist){
				 intIndexPoolidxValueCode = intIndexPool.create(idxCode,valueCode);
			 } else {
				 /* already exist */
				 intIndexPoolidxValueCode = intIndexPool.getIndex(valueCode);
			 }
			 
		 } else {
			 /* if dont exist then creat*/
			 intIndexPoolidxValueCode = intIndexPool.create(idxCode,valueCode);
		 }
		 
		 return intIndexPoolidxValueCode;
	}

	
	public int addJoin( int idxCode, String idxValue, OffHeapInt intIndexPool, OffHeapStr strPool ){
		 int intIndexPoolidxValueCode = -1; 
		 int idxStrPoolCode = strPool.getIndex(idxValue.getBytes());
	
		 if(idxStrPoolCode <=-1){ // idxValue doesn't  exist in StringPool
			 idxStrPoolCode = strPool.create(idxValue.getBytes());
		 } 
		 
		 if( intIndexPool.exist(idxStrPoolCode) ) { // check if idxStrPoolCode exist in intIndexPool
			 
			 int[] kS =   intIndexPool.getKs(idxStrPoolCode);
			 boolean exist = false;
			 for (int i = 0; i < kS.length; i++) {
				int j = kS[i];
				if(idxCode == j){
					exist = true;
				}
			}
			 
			 if(!exist){
				 intIndexPoolidxValueCode = intIndexPool.create(idxCode,idxStrPoolCode);
			 } else {
				 /* already exist */
				 //intIndexPoolidxValueCode = intIndexPool.getIndex(idxStrPoolCode);
				 return idxStrPoolCode;
			 }
			 
			 
			 
		 } else {
			 /* if dont exist then creat*/
			 intIndexPoolidxValueCode = intIndexPool.create(idxCode,idxStrPoolCode);
		 }
		 return intIndexPoolidxValueCode;
	}
	
	
	public int addStrPool( byte[] value, OffHeapStr strPool ){
		 int strPoolValueCode = -1; 
		 
		 if( strPool.exist(value) ) { // check if idxStrPoolCode exist in intIndexPool
			 /* already exist */
			 strPoolValueCode = strPool.getIndex(value);
			
		 } else {
			 /* if dont exist then creat*/
			 strPoolValueCode = strPool.create(value);
		 }
		 return strPoolValueCode;
	}
    
	@Test
	public void indexMoveTo(){
		OffHeapInt orgIndex =  orgIndex = new Index(3);
				   orgIndex.jump(2);  
				   orgIndex.setIndex(122);;
				   Assert.assertEquals( orgIndex.getIndex(),122);
	}
	
	@Test
	public void indexZero(){
		OffHeapInt orgIndex =  orgIndex = new Index(100);
				   orgIndex.jump(6);  
				   orgIndex.setIndex(34);
				   orgIndex.setKey(38);
				   Assert.assertEquals( orgIndex.getIndex(),34);
				   Assert.assertEquals( orgIndex.getKey(6),38);
				   
				   orgIndex.jump(0);  
				   orgIndex.setIndex(0);
				   orgIndex.setKey(38);
				   Assert.assertEquals( orgIndex.getIndex(),0);
				   Assert.assertEquals( orgIndex.getKey(6),38);
				  
				   /*TODO must implement NULL FLAG in the OffHeapObject to check if object isNull or not, 
				    * seed counter must reuse/use unused index number
				    * */
				   addIndex(2, 2, orgIndex);
				   Assert.assertNotSame( orgIndex.getKey(2),2);
				   
	}
}
