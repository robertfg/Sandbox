package memory.cache;

public class TestUnsafe {

	public static void main(String[] args) {
	   int memSize = 1000 * 1000 * 1000;
	   int objSize = 100;
	   int numOfRecords = memSize / objSize;
		
	   System.out.println( "Number of Records:" + numOfRecords );
		UnsafeCache<User,User> uCache = new UnsafeCache<User, User>( objSize, memSize);
		
		
		
		for (int i = 0; i < numOfRecords; i++) {
		    User key = new User( i,"A","Email1");
			uCache.put( key, new User(i,"A","Email2"));
		}
		//	User usr2 = uCache.get(key);
			//  System.out.println(usr2.getEmail()); 

		printMemory();
		System.gc();
		printMemory();
		
	}
	
	public static void printMemory(){
		
		  int mb = 1024*1024;
	         
	        //Getting the runtime reference from system
	        Runtime runtime = Runtime.getRuntime();
	         
	        System.out.println("##### Heap utilization statistics [MB] #####");
	         
	        //Print used memory
	        System.out.println("Used Memory:"
	            + (runtime.totalMemory() - runtime.freeMemory()) / mb);
	 
	        //Print free memory
	        System.out.println("Free Memory:"
	            + runtime.freeMemory() / mb);
	         
	        //Print total available memory
	        System.out.println("Total Memory:" + runtime.totalMemory() / mb);
	 
	        //Print Maximum available memory
	        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
	}

	
	
}
