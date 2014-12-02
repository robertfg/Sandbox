package tradelifecycle;
/*import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class TradeTest {

	private TradePlacing tradePlacing;
	private Trade output;
	
	@Before
	public void setUp() throws Exception {
		tradePlacing = new TradePlacing();
		tradePlacing.place( new Trade( 1234,1, "XYZ",100,  Direction.BUY,"ACC-1234",Operation.NEW) );
		tradePlacing.place( new Trade( 1234,2, "XYZ",150,  Direction.BUY,"ACC-1234",Operation.AMEND) );
		
		
		tradePlacing.place( new Trade( 5678,1, "QED",200,  Direction.BUY,"ACC-2345",Operation.NEW)    );
		tradePlacing.place( new Trade( 7897,2, "QED",0,    Direction.BUY,"ACC-2345",Operation.CANCEL) );
		
		
		tradePlacing.place( new Trade( 2233,1, "RET",100,  Direction.SELL,"ACC-3456",Operation.NEW)    );
		tradePlacing.place( new Trade( 2233,2, "RET",400,  Direction.SELL,"ACC-3456",Operation.AMEND) );
		tradePlacing.place( new Trade( 2233,3, "RET",0,    Direction.SELL,"ACC-3456",Operation.CANCEL) );
		
		
		tradePlacing.place( new Trade( 8896,1, "YUI",300,  Direction.BUY,   "ACC-4567",Operation.NEW)    );
		tradePlacing.place( new Trade( 6638,1, "YUI",100,    Direction.SELL,"ACC-4567",Operation.NEW) );
		
	
		tradePlacing.place( new Trade( 6363,1, "HJK",200,  Direction.BUY,    "ACC-5678",Operation.NEW)    );
		tradePlacing.place( new Trade( 7666,1, "HJK",200,    Direction.BUY , "ACC-5678",Operation.NEW) );
		tradePlacing.place( new Trade( 6363,2, "HJK",100,  Direction.BUY,    "ACC-5678",Operation.AMEND)    );
		tradePlacing.place( new Trade( 7666,2, "HJK",50,    Direction.SELL,  "ACC-5678",Operation.AMEND) );
		
		tradePlacing.place( new Trade( 8686,1, "FVB",100,    Direction.BUY , "ACC-6789"   ,Operation.NEW) );
		tradePlacing.place( new Trade( 8686,2, "GBN",100,    Direction.BUY,    "ACC-6789" ,Operation.AMEND)    );
		tradePlacing.place( new Trade( 9654,1, "FVB",200,    Direction.BUY,  "ACC-6789"  , Operation.NEW) );
		
		
		tradePlacing.place( new Trade( 1025,1, "JKL",100,    Direction.BUY ,   "ACC-7789"   ,Operation.NEW) );
		tradePlacing.place( new Trade( 1036,1, "JKL",100,    Direction.BUY,    "ACC-7789" ,Operation.NEW)    );
		tradePlacing.place( new Trade( 1025,2, "JKL",100,    Direction.SELL,   "ACC-8877"  , Operation.AMEND) );
		
		
		tradePlacing.place( new Trade( 1122,1, "KLO",100,    Direction.BUY ,  "ACC-9045"   ,Operation.NEW) );
		tradePlacing.place( new Trade( 1122,1, "HJK",100,    Direction.SELL,  "ACC-9045" ,Operation.NEW)    );
		tradePlacing.place( new Trade( 1122,2, "KLO",100,    Direction.SELL,  "ACC-9045"  , Operation.AMEND) );
		tradePlacing.place( new Trade( 1144,1, "KLO",300,    Direction.BUY ,  "ACC-9045"   ,Operation.NEW) );
		tradePlacing.place( new Trade( 1144,1, "KLO",400,    Direction.BUY,   "ACC-9045" ,Operation.NEW)    );
		tradePlacing.place( new Trade( 1155,2, "KLO",600,    Direction.SELL,  "ACC-9045"  , Operation.AMEND) );
		tradePlacing.place( new Trade( 1155,2, "KLO",0,      Direction.BUY,   "ACC-9045"  , Operation.AMEND) );
		
		
	}

    @Test
	public void tradeEvents(){
		
    	        output =  tradePlacing.aggregate("ACC-1234","XYZ", new int[]{1234});
		        assertEquals(150, output.getQuantity());
		       
			    output =  tradePlacing.aggregate("ACC-2345","QED", new int[]{5678,7897} );
		        assertEquals(0, output.getQuantity());
		      
		        output =  tradePlacing.aggregate("ACC-3456","RET", new int[]{2233});
		        assertEquals(0, output.getQuantity());
		      
		        
		        output =  tradePlacing.aggregate("ACC-4567","YUI", new int[]{8896,6638});
		        assertEquals(200, output.getQuantity());
		      
		        output =  tradePlacing.aggregate("ACC-5678","HJK", new int[]{6363,7666});
		        assertEquals(50, output.getQuantity());
		              
		        output =  tradePlacing.aggregate("ACC-6789","GBN", new int[]{8686});
		        assertEquals(100, output.getQuantity());
		        
		        output =  tradePlacing.aggregate("ACC-6789","FVB", new int[]{9654});
		        assertEquals(200, output.getQuantity());
		        
		        output =  tradePlacing.aggregate("ACC-7789","JKL", new int[]{1036});
		        assertEquals(100, output.getQuantity());
		        
		        output =  tradePlacing.aggregate("ACC-8877","JKL", new int[]{1025});
		       // assertEquals(-100, output.getQuantity());
		        
		        
		        output =  tradePlacing.aggregate("ACC-9045","KLO", new int[]{1122,1144,1155});
		        assertEquals(300, output.getQuantity());
		        
		        output =  tradePlacing.aggregate("ACC-9045","HJK", new int[]{1122});
		        assertEquals(0, output.getQuantity());
		         
		
	}
}
*/