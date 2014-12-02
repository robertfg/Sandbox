package eye;



public class Test {

	public static void main(String[] args){
//		  final OffHeapStr t = new LookUpStr(2);
//		  t.setIndex(0);
//		 // t.setValue("value".getBytes());
//		  
//		  System.out.println(t.getIndex());
//		  System.out.println( t.getValue() );
		 final OffHeapInt orgName = new Index(2);
		 
		 final OffHeapStr strPool = new LookUpStr(2);
		 				  strPool.moveTo(0);
						  strPool.setIndex(1);
						  strPool.setValue("value christopher".getBytes());
										 
						  strPool.moveTo(1);
						  strPool.setIndex(1111);
						  strPool.setValue("1111".getBytes());
		 
		 System.out.println(  strPool.exist( "11cc11".getBytes()  ) );
		 
						
	}
	
	
}
