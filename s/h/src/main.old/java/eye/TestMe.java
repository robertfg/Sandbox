package eye;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestMe {
    
	public LookupCacheProvider lokupProvider = new LookupCacheProvider();
	
	

	@Before
	public void setUp() throws Exception {
		String json = "c:\\devs\\sample.json";
		 json = "/home/degs/devs/space/eyeota/src/test/resources/data1.json";
				
		lokupProvider.loadData(json);
	}


	@Test
	public void test() {
		
		SegmentConfig[] segment =  lokupProvider.getSegmentFor("org1", "paramName1"); //will return an empty SegmentConfig array. 
		
		Assert.assertArrayEquals(new SegmentConfig[]{}, segment);
		segment = null;
		
		segment = lokupProvider.getSegmentFor("org1", "paramName1", "paramVal1");// will return a 1-element SegmentConfigArray containing a SegmentConfig object for seg_1234
		
		Assert.assertEquals("seg_1234", segment[0].getSegmentId());
		
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "paramName1", "paramVal2" ); // will return a 1-element SegmentConfigArray containing a SegmentConfig object with id: "intr.edu"
	
		Assert.assertEquals("intr.edu", segment[0].getSegmentId());
		
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "paramName1", "paramVal3"  ); // will return a 1-element SegmentConfigArray containing a SegmentConfig object with id: "intr.edu"
		Assert.assertEquals("intr.edu", segment[0].getSegmentId());
		 
		
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "paramName1",  "paramVal4" ); // will return a 1-element SegmentConfigArray containing a SegmentConfig object with id: "intr.edu"
		Assert.assertEquals("intr.edu", segment[0].getSegmentId());
		
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "paramName1",  "paramVal5" ); // will return a 1-element SegmentConfigArray containing a SegmentConfig object with id: "intr.edu"
		Assert.assertEquals("intr.edu", segment[0].getSegmentId());
		
		
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "paramName1", "paramVal6" );// will return a 3-element SegmentConfigArray containing SegmentConfig objects with ids: dem.infg.m, intr.heal, dem.infg.f
		Assert.assertEquals(3, segment.length);
		
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "paramName1", "paramVal6" );// will return a 3-element SegmentConfigArray containing SegmentConfig objects with ids: dem.infg.m, intr.heal, dem.infg.f
		Assert.assertEquals(3, segment.length);
		
	
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "testedu");// will return a 1-element SegmentConfigArray containing a SegmentConfig object with id "n277"
		
		Assert.assertEquals(1, segment.length);
		Assert.assertEquals("n277", segment[0].getSegmentId());
		
		

		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "testedu", "");// will return a 1-element SegmentConfigArray containing a SegmentConfig object with id "n277" 
		Assert.assertEquals(1, segment.length);
		Assert.assertEquals("n277", segment[0].getSegmentId());
		
		
		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "testedu", "christopher");// will return an empty SegmentConfig array. 
		
		

		segment = null;
		segment = lokupProvider.getSegmentFor("org1", "gen", "Female");// will return a 1-element SegmentConfigArray containing a SegmentConfig object with id "dem.g.f" 
	
				segment = null;
				segment = lokupProvider.getSegmentFor("org1", "gen", "Male");// will return a 1-element SegmentConfigArray containing a SegmentConfig object with id "dem.g.m" 
	

			segment = null;
			segment = lokupProvider.getSegmentFor("org1", "gen", "christopher"); //will return an empty SegmentConfig array.
			for (int i = 0; i < segment.length; i++) {
				System.out.println( "www:" +segment[i].getSegmentId() );
	  		}
 		
	}
	
	
	

}
