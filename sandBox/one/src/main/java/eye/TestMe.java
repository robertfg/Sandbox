package eye;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMe {
    
	public LookupCacheProvider lokupProvider = new LookupCacheProvider();
	
	

	@Before
	public void setUp() throws Exception {
		String json = "c:\\devs\\sample.json";
		lokupProvider.loadData(json);
	}


	@Test
	public void test() {
		
		SegmentConfig[] segment =  lokupProvider.getSegmentFor("org1", "paramName1"); //will return an empty SegmentConfig array. 
		
		Assert.assertArrayEquals(new SegmentConfig[]{}, segment);
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "paramName1", "paramVal1");// will return a 1-element SegmentConfigArray containing a SegmentConfig object for seg_1234
		
		Assert.assertEquals("seg_1234", segment[0].getSegmentId());
		
		//getSegmentFor("org1", "paramName1", "paramVal2 | paramVal3" | "paramVal4" | "paramVal5" ); // will return a 1-element SegmentConfigArray containing a SegmentConfig object with id: "intr.edu"
		
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "paramName1", "paramVal6" );// will return a 3-element SegmentConfigArray containing SegmentConfig objects with ids: dem.infg.m, intr.heal, dem.infg.f
		Assert.assertEquals(3, segment.length);
		
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "paramName1", "paramVal6" );// will return a 3-element SegmentConfigArray containing SegmentConfig objects with ids: dem.infg.m, intr.heal, dem.infg.f
		Assert.assertEquals(3, segment.length);
		
	
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "testedu");// will return a 1-element SegmentConfigArray containing a SegmentConfig object with id "n277"
		for (int i = 0; i < segment.length; i++) {
			System.out.println( "www:" +segment[i].getSegmentId() );
  		}
		
		//The query getSegmentFor("org1", "testedu", "") will return a 1-element SegmentConfigArray containing a SegmentConfig object with id "n277" 
		
		
/*
The query  
The query getSegmentFor("org1", "paramName1", <<"paramVal2" | "paramVal3" | "paramVal4" | "paramVal5">> ) will return a 1-element SegmentConfigArray containing a SegmentConfig object with id: "intr.edu" 
The query  
The query getSegmentFor("org1", "testedu") will return a 1-element SegmentConfigArray containing a SegmentConfig object with id "n277"
The query getSegmentFor("org1", "testedu", "") will return a 1-element SegmentConfigArray containing a SegmentConfig object with id "n277" 
The query getSegmentFor("org1", "testedu", <<any value other than an empty string>>) will return an empty SegmentConfig array. 
The query getSegmentFor("org1", "gen", "Female") will return a 1-element SegmentConfigArray containing a SegmentConfig object with id "dem.g.f" 
The query getSegmentFor("org1", "gen", "Male") will return a 1-element SegmentConfigArray containing a SegmentConfig object with id "dem.g.m" 
The query getSegmentFor("org1", "gen", <<any value other than "Male" or "Female">>) will return an empty SegmentConfig array.

		 * */
	}
	
	
	

}
