package searching;

import java.util.BitSet;

public class BitSetDemo {

	  public static void main(String args[]) {
	  
		  String str = "";
		  System.out.println(str.hashCode());
		  
		  
		  //	BitSetDemo.bitsToString();
		  	BitSetDemo.bitSetOperation();
	     
	  }

      public static void getBit(){
    	  
    	  
      }
      
      

	  public static void testInsert() {
		  
		  long start = System.currentTimeMillis();
		     BitSet bits1 = new BitSet(16);
		     BitSet bits2 = new BitSet(16);
		     
		     bits1.set(1);
		     
	  }
      
	  public static void bitSetOperation() {
		  
		  long start = System.currentTimeMillis();
		     XBit bits1 = new XBit(16);
		     XBit bits2 = new XBit(16);
		     XBit bits3 = new XBit(16);
		     XBit bits4 = new XBit(16);
		        
		     // set some bits
		     for(int i=0; i < 10; i++) {
		        if((i%2) == 0) bits1.set(i);
		        			   bits1.setMap(true);	
		        if((i%5) != 0) bits2.set(i);
		        if((i%3) != 0) bits3.set(i);
		        if((i%7) != 0) bits4.set(i);
		     }
		     
		    System.out.println("Initializing pattern in bits1: ");
		   System.out.println(bits1);
		    System.out.println("\nInitializing pattern in bits2: ");
		   // System.out.println(bits2);
		    System.out.println("\nInitializing pattern in bits3: ");
		  //  System.out.println(bits3);

		    
		     // AND bits
		     bits2.and(bits1);
		     System.out.println("\nbits2 AND bits1: " + (( System.currentTimeMillis() - start)) + " ms");
		   //  System.out.println(bits2);
		     
		     bits2.and(bits3);
		     System.out.println("\nbits2 AND bits3: " + (( System.currentTimeMillis() - start))  + " ms");
		   //  System.out.println(bits2);
		     
		     bits2.and(bits4);
		     System.out.println("\nbits2 AND bits4: " + (( System.currentTimeMillis() - start))  + " ms");
		  

		     System.out.println( "bits1 cardinality:" + bits1.cardinality() );
		     System.out.println( "bits2 cardinality:" + bits2.cardinality() );
		     System.out.println( "bits3 cardinality:" + bits3.cardinality() );
		     System.out.println( "bits4 cardinality:" + bits4.cardinality() );

		     System.out.println(bits1.get(0));
		     
		     
		     
		     // OR bits
		 //    bits2.or(bits1);
		  //   System.out.println("\nbits2 OR bits1: ");
		  //   System.out.println(bits2);

		     // XOR bits
		  //   bits2.xor(bits1);
		 //    System.out.println("\nbits2 XOR bits1: ");
		     
		     
		     
		//     System.out.println(bits2);
		     
		  }

	  public static void bitsToString() {

	      // create 2 bitsets
	      BitSet bitset1 = new BitSet(8);
	      BitSet bitset2 = new BitSet(8);

	      // assign values to bitset1
	      bitset1.set(0);
	      bitset1.set(1);
	      bitset1.set(2);
	      bitset1.set(3);
	      bitset1.set(4);
	      bitset1.set(5);

	      // assign values to bitset2
	      bitset2.set(2);
	      bitset2.set(4);
	      bitset2.set(6);
	      bitset2.set(8);
	      bitset2.set(10);

	      // print the sets
	      System.out.println("Bitset1:" + bitset1);
	      System.out.println("Bitset2:" + bitset2);

	      // print the string representations of the bitsets
	      System.out.println("" + bitset1.toString().replaceAll("0","yanni"));
	      System.out.println("" + bitset2.toString());
	   }
}
