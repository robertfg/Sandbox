package searching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchUtil {
	
	private BitSet col1 = new BitSet(16);
	
	public static void main(String[] args) {
		
		SearchUtil searchUtil = new SearchUtil();
//		BitSet bitSet = new BitSet();
//				   searchUtil.generateData(bitSet, 10, 0);
		SearchUtil.test();
	}
	
	
	private void Search(String strSearch, BitSet column){
		
		//if( isPresent( strToBit(strSearch),column ) ){
			
		//}
			
			
	}
	
	private int strToBit(String strSearch) {
	  return strSearch.hashCode();	 
	}
	
	private boolean isPresent( BitSet bitSet,int bitIndex){
		return bitSet.get(bitIndex);
	}
	
	private void insert(BitSet bitSet, int bitIndex){
		if(!isPresent(bitSet,bitIndex)){
			bitSet.set(bitIndex);	
		} 
	}
	
	private BitSet generateData(BitSet bitSet, int cardinality, int ctr){
	         if(ctr==cardinality){
	        	 return bitSet;
	         }	else {
	        	 insert(bitSet, strToBit("A:" + ctr  )  ) ;
	        	 System.out.println( bitSet.cardinality() );
	        	 ctr++;
	        	 return generateData( bitSet, cardinality, ctr);
	         }
	}
	
	public static void test(){
		
		

		 
		 Map<String,SegmentConfig> paramName1 = new HashMap<String, SearchUtil.SegmentConfig>();
		 paramName1.put("paramVal1", new SegmentConfig( "dem.infg.m1"));
		 paramName1.put("paramVal2", new SegmentConfig( "dem.infg.m2"));
		 paramName1.put("paramVal3", new SegmentConfig( "dem.infg.m3"));
		 paramName1.put("paramVal4", new SegmentConfig( "dem.infg.m4"));
		 paramName1.put("paramVal2\nparamVal23\nparamVal4\nparamVal5", new SegmentConfig( "dem.infg.mX"));
		 
		 Map<String,SegmentConfig> testedu = new HashMap<String, SearchUtil.SegmentConfig>();	
		 							testedu.put("", new SegmentConfig( "dem.infg.mX"));
		
		 Map<String,SegmentConfig> sid = new HashMap<String, SearchUtil.SegmentConfig>();
		 sid.put(" ", new SegmentConfig( "dem.life.expat"));
		 
		 
		 Map<String,SegmentConfig> gen = new HashMap<String, SearchUtil.SegmentConfig>();
		 						   gen.put("Female", new SegmentConfig( "dem.life.expat"));
		 						   gen.put("Male",   new SegmentConfig( "dem.life.expat"));
		 
		 
		 

		List<Map<String,SegmentConfig>> segmentArray = new ArrayList<Map<String, SearchUtil.SegmentConfig>>();
	
		 	segmentArray.add(paramName1);
		 	segmentArray.add(testedu);
		 	segmentArray.add(sid);
		 	segmentArray.add(gen);
		 	
		 	
		 //	<<"paramVal2" | "paramVal3" | "paramVal4" | "paramVal5">>
		 	
	
		 	Map<String,List< Map<String,SegmentConfig>>> segmentMap = new HashMap<String,List< Map<String, SegmentConfig>>>();
		 	segmentMap.put("paramName1", segmentArray);
		 	
		 	List<Map<String,List< Map<String,SegmentConfig>>>> segmentArrayMap = new ArrayList<Map<String,List< Map<String, SegmentConfig>>>>();
		 	segmentArrayMap.add(segmentMap);
		
		 	Map<String,List<Map<String,List< Map<String,SegmentConfig>>>>> segmentMapArrayMap = new HashMap<String,List<Map<String,List< Map<String, SegmentConfig>>>>>();
		 	segmentMapArrayMap.put("org1", segmentArrayMap);
		
		 
			Map<String,List<Map<String,List< Map<String,SegmentConfig>>>>> segmentMapArrayMap2 = new HashMap<String,List<Map<String,List< Map<String, SegmentConfig>>>>>();
		 	segmentMapArrayMap2.put("org2", segmentArrayMap);
		
		 	
		 	List<Map<String,List<Map<String,List< Map<String,SegmentConfig>>>>>> segmentListMapArrayMap = new ArrayList<Map<String,List<Map<String,List< Map<String, SegmentConfig>>>>>>();
		 	segmentListMapArrayMap.add(segmentMapArrayMap);
		 	segmentListMapArrayMap.add(segmentMapArrayMap2);
		 	
		 	
		 	
		 	String orgKey = "org1";
		 	String paramKey = "paramName1";
		 	String paramValKey = "paramVal6";
		 	
		 	//System.out.println( "Get from the array:" + segmentListMapArrayMap.get( 0 ) );
		 	
		 	Map<String,List<Map<String,List< Map<String,SegmentConfig>>>>> map1 = segmentListMapArrayMap.get( 0 ) ;  // 
		 	
		 	System.out.println(  map1.get("org1").get(0).get("paramName1").get(0)  );
		 	
		 	
		 	//System.out.println( segmentListMapArrayMap.get( 0 /*segmentListMapArrayMap.indexOf(orgKey)  can change to map lookup*/) .get(orgKey).g /*.get(0 lookup )*/  );
		 
		    ObjectMapper mapper = new ObjectMapper();
		
		try {
			mapper.writeValue(System.out, segmentListMapArrayMap);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	 public SegmentConfig[] getSegmentFor(final String orgKey, final String paramKey){
		 
		 StringBuilder json =  new StringBuilder( "[{\"" + orgKey + ":[");
		 json.append("{\"" + paramKey + "\":" );
		 json.append("["); 
		 /*	
			    { "paramVal1": { "segmentId": "seg_1234" } }, 
				{ "paramVal2\nparamVal23\nparamVal4\nparamVal5": { "segmentId": "intr.edu" } }, 
				{ "paramVal6": { "segmentId": "dem.infg.m" }}, 
				{ "paramVal6": { "segmentId": "intr.heal" }}, 
				{ "paramVal6": { "segmentId": "dem.infg.f" }}
		 */	
			json.append("]");
			json.append("}");
		 
		 json.append("]}]");
		 
		 return null;
		 
	 }
	 
	 public SegmentConfig[] getSegmentFor(final String orgKey, final String paramKey, final String paramValKey){
		 
		 return null;
	 }
	private static class SegmentConfig{
		
		public SegmentConfig(String segmentId) {
			this.segmentId = segmentId;
		}

		private String segmentId;

		public String getSegmentId() {
			return segmentId;
		}

		public void setSegmentId(String segmentId) {
			this.segmentId = segmentId;
		}
		
	}

}
