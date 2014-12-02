package com.anz.util;

import java.util.ArrayList;
import java.util.List;



public class Sizeof
{
 public static void main (String [] args) throws Exception
 {
     runGC ();
     usedMemory ();
     
     // array to keep strong references to allocated objects:
       final int count = 7000000; // 10000 or so is enough for small ojects
       			List<Indexer> file1 = new ArrayList<Indexer>();
       			List<Indexer> file2 = new ArrayList<Indexer>();
       			
       			List<CsvLine> csvs1 = new ArrayList<CsvLine>();
       			List<CsvLine> csvs2 = new ArrayList<CsvLine>();
       	     
     
     
     long heap1 = 0;
     // allocate count+1 objects, discard the first one:
     for (int i = -1; i < count; ++ i)
     {
        // Indexer object; 
    	// Indexer index;
         // INSTANTIATE YOUR DATA HERE AND ASSIGN IT TO 'object':
         
        // object = new Indexer("1234567890-1234567890-1234567890abcdefghijklmnop",9); // 8 bytes
         //object = new Integer (i); // 16 bytes
         //object = new Long (i); // same size as Integer?
         //object = createString (10); // 56 bytes? fine...
         //object = createString (9)+' '; // 72 bytes? the article explains why
         //object = new char [10]; // 32 bytes
        // object = new byte [32][1]; // 656 bytes?!
       
         
    	 if (i >= 0){
        	 file1.add( new Indexer("1234567890-1234567890-1234567890abcdefghijklmnop",i) ); 
        	 csvs1.add(new CsvLine(
        			 "1234567890-1234567890-1234567890abcdefghijklmnop," +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop," +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop," +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 " 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop," +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop, +" +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop " +
        			 "1234567890-1234567890-1234567890abcdefghijklmnop","1:" + i)  );

        	 
        	 file2.add( new Indexer("1234567890-1234567890-1234567890abcdefghijklmnop",i) ); 
        	 csvs2.add(new CsvLine(
"1234567890-1234567890-1234567890abcdefghijklmnop," +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop," +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop," +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
" 1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop," +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop, +" +
"1234567890-1234567890-1234567890abcdefghijklmnop " +
"1234567890-1234567890-1234567890abcdefghijklmnop","1:" + i)  );
         }else {
        	 //file1 = null;
        	 //csvs1 = null;
             runGC ();
             heap1 = usedMemory (); // take a "before" heap snapshot
         }
     }

     runGC ();
     long heap2 = usedMemory (); // take an "after" heap snapshot:
     
     final int size = Math.round (((float)(heap2 - heap1))/count);
     System.out.println ("'before' heap: " + heap1 +
                         ", 'after' heap: " + heap2);
    
     System.out.println("" + file1.size());
     System.out.println("" + file2.size());
     System.out.println("" + csvs1.size());
     System.out.println("" + csvs2.size());
     
   System.out.println ("heap delta: " + (heap2 - heap1) +
       ", {" + file1.get(0).getClass () + "} size = " + size + " bytes");
     
 }
 
 // a helper method for creating Strings of desired length
 // and avoiding getting tricked by String interning:
 public static String createString (final int length)
 {
     final char [] result = new char [length];
     for (int i = 0; i < length; ++ i) result [i] = (char) i;
     
     return new String (result);
 }

 // this is our way of requesting garbage collection to be run:
 // [how aggressive it is depends on the JVM to a large degree, but
 // it is almost always better than a single Runtime.gc() call]
 private static void runGC () throws Exception
 {
     // for whatever reason it helps to call Runtime.gc()
     // using several method calls:
     for (int r = 0; r < 4; ++ r) _runGC ();
 }

 private static void _runGC () throws Exception
 {
     long usedMem1 = usedMemory (), usedMem2 = Long.MAX_VALUE;

     for (int i = 0; (usedMem1 < usedMem2) && (i < 1000); ++ i)
     {
         s_runtime.runFinalization ();
         s_runtime.gc ();
         Thread.currentThread ().yield ();
         
         usedMem2 = usedMem1;
         usedMem1 = usedMemory ();
     }
 }

 private static long usedMemory ()
 {
     return s_runtime.totalMemory () - s_runtime.freeMemory ();
 }
 
 
 
 
 private static final Runtime s_runtime = Runtime.getRuntime ();

} // end of class
//----------------------------------------------------------------------------
