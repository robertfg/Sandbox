package indexer;

import java.lang.reflect.Field;


import sun.misc.Unsafe;


public class Indexer {

  private  final int NUM_RECORDS ;
	
  private static long address;
  private static final Index idx = new Index();

  
  private static final Unsafe unsafe;
  static
  {
      try
      {
          Field field = Unsafe.class.getDeclaredField("theUnsafe");
          field.setAccessible(true);
          unsafe = (Unsafe)field.get(null);
      }
      catch (Exception e)
      {
          throw new RuntimeException(e);
      }
  }
  
  public Indexer( int NUM_RECORDS) {
	  this.NUM_RECORDS = NUM_RECORDS;
	  create();
  }
  
  
  private  void create() {
      final long requiredHeap = NUM_RECORDS * Index.getObjectSize();
      address = unsafe.allocateMemory(requiredHeap);
  }
  
  public Index alloc (final int index)  {
      final long offset = address + (index * Index.getObjectSize());
      idx.setObjectOffset(offset); 
      idx.setMemRegion(unsafe);
      return idx;
  }
  
  public Index get (final int index)  {
     return alloc(index);
  }

  public static void TestMe(){
	  
	  Indexer indexer = new Indexer(2);
	  	  Index  index = indexer.alloc(0);
	     	  	
	     	     
	             index.setIndex(0);
	             index.setQuantity(777);
	             index.setTradeId(37);
	  	         index.setValueCode(41);
			     index.setInstrumentCode(56);
	  	 
	  
	     Index readIndex = indexer.get(0);
	     	System.out.println( "Index:"    +  readIndex.getIndex()    );
	     	System.out.println( "Quantity:" +  readIndex.getQuantity() );
	     	System.out.println( "TradeID:"  +  readIndex.getTradeId()  );
	     	System.out.println( "ValueCode:" + readIndex.getValueCode() );
	    	System.out.println( "ValueCode:" + readIndex.getInstrumentCode() );
		  	 
  }
   
  public static void main(String[] args) {
	
	   
	  Indexer.TestMe();
	
   
  }

}
