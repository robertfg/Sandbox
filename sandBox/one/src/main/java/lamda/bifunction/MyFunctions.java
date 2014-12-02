package lamda.bifunction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;







public class MyFunctions {

	public static void main(String[] args) {
		
	    MyFunctions.intStream();
		MyFunctions.mapReduce();
		
		MyFunctions.combiningFunctionExample();
	
		
		MyFunctions.lamdaSortString();
		
		MyFunctions.runnable();
		MyFunctions.listForEach();
		MyFunctions.lamdaComparator();
		MyFunctions.staticReferencingExample();
		MyFunctions.instanceCapturingExample();
		MyFunctions.biFunction();
	
	}

	
	public static void biFunction(){
			
		     BiFunction<Integer,Integer,Integer> addition = ( x, y ) -> x + y;
		 	 System.out.println( "calling addition of 2 and 3 resulting: " + addition.apply( 2, 3 ) );

		 	// Java 7 form.
		 	BiFunction<String,String,Integer> bf1 = new BiFunction<String,String,Integer>() {
		 	  public Integer apply(String self, String arg) {
		 	    return self.indexOf(arg);
		 	  }
		 	};

		 	// Java 8 standard lambda.
		 	BiFunction<String,String,Integer> bf2 = (str,i) -> str.indexOf(i);
		 	System.out.println(bf2.apply("christopher", "christ"));

		 	
		 	// Method reference:
		 	BiFunction<String,String,Integer> bf3 = String::indexOf;
		 	System.out.println(bf3.apply("christopher", "christ"));
		 	
	}
	
	public static void runnable(){
		Runnable r = ( ) -> System.out.println( "run run run" );
		Thread t = new Thread( r );
		t.start();

	}
	
	public static void listForEach(){
		Arrays.asList( "element", "other", "another one" ).forEach( e -> System.out.println( e ) );
	}

	public static void lamdaComparator(){
		
		List names = Arrays.asList( "prado", "gugenheim", "reina sofia", "louvre" );
		
		Collections.sort( names, ( String a, String b ) -> b.compareTo( a ) );
						  names.forEach( x -> System.out.println( x ) );
		
	
						 
							
	
	
	}
	
	
	public static void lamdaSortString(){
		
		
		List<String> names = Arrays.asList( "+15%","+30%","-15%","-15.00%","-20.00%","-30%","-30.00%","15.00%","30.00%");
		
		Collections.sort( names, ( String fxSpot1, String fxSpot2 ) -> {
			fxSpot1 =  fxSpot1.trim().replace("%", "").replace("00", "").replace(".", "");
			fxSpot2 = fxSpot2.trim().replace("%", "").replace("00", "").replace(".", "");
			
					return new Integer(fxSpot1).compareTo(new Integer(fxSpot2));
			
					
					 
			
			}
		);
		
						  names.forEach( x -> System.out.println( x ) );
						  
	
						  Collections.sort(names, new Comparator<String>(){
							  
							    public int compare(String p1, String p2){
							  
							      
							    	return p1.compareTo(p2);
							  
							    }
							  		  });
		
	}
	
	
	
	public static void staticReferencingExample(){
		
		// The Java 7 way - wish this syntax a fond farewell!
		Function<String,Integer> f1 = new Function<String,Integer>() {
		  public Integer apply(String x) {
		    return Integer.valueOf(x);
		  }
		};

		System.out.println( f1.apply("1") );
	
		
		
		// The naive (and overly verbose) Java 8 way.
		Function<String,Integer> f2 = (String str) -> {
		  return Integer.valueOf(str);
		};
		
		System.out.println( f2.apply("1") );
		
		
		// The inference-y short-hand naive Java 8 way.
		Function<String,Integer> f3 = (str) -> Integer.valueOf(str);
		
		Function<Integer,String> f5 = (str) ->  {
			 									 return "int to string " + String.valueOf(str);
			 									};
	  System.out.println( f5.apply(100)  ); 
			 									
		
		System.out.println( f3.apply("1") ); 
		
		
		// The method reference Java 8 way.
	    Function<String,Integer> f4 = Integer::valueOf;
	    System.out.println( f4.apply("455"));
	}
	
	public static void instanceCapturingExample(){
		
		Integer x = new Integer(123);
		
		// Create a string supplier off of the instance Java 7 style.
		Supplier<String> s1 = new Supplier<String>() {
		  public String get() {
		    return x.toString();
		  }
		};
		
		

		// Short-hand lambda version.
		Supplier<String> s2 = () -> x.toString();

		System.out.println( s2.get() );
		
		// Method reference version.
		Supplier<String> f2 = x::toString;
		System.out.println(f2.get());
		
	}
	
	
	public static void combiningFunctionExample(){
	//http://stackoverflow.com/questions/18400210/java-8-where-is-trifunction-and-kin-in-java-util-function-or-what-is-the-alt
		
		Function<Integer, Function<Integer, UnaryOperator<Integer>>> tri1 = a -> b -> c -> a + b + c;
		System.out.println(tri1.apply(1).apply(2).apply(3)); //prints 6
		
		
		
		BiFunction<Integer, Integer, UnaryOperator<Integer>> tri2 = (a, b) -> c -> a + b + c;
		System.out.println(tri2.apply(1, 2).apply(3)); //prints 6
		
		//partial function can be, of course, extracted this way
		UnaryOperator partial = tri2.apply(1,2); //this is partial, eq to c -> 1 + 2 + c;
		System.out.println(partial.apply(4)); //prints 7
		System.out.println(partial.apply(5)); //prints 8
		
		
		
		
	}
	
	
	public static void mapReduce(){
		
		Arrays.asList(1,2,3,4,5,6).stream().map(n -> n * n) // square the number
	      .map(n -> n + 3) // add 3 to the value
	      .reduce(0, (i, j) -> i + j); // add the number to the accumulator
	      
		
	}
	
	public static void pipelining(){
		
		List<String> result =  Arrays.asList("Larry", "Moe", "Curly")
                .stream()
                .map(s -> "Hello " + s)
                .collect(Collectors.toList());
		
		/*
		 * 
		 * Let us evaluate the steps in the pipeline
				Arrays.asList("Larry", "Moe", "Curly"): instantiates a list with three strings viz. "Larry", "Moe" and "Curly".
				.stream(): Converts the list into a stream
				.map(s -> "Hello " + s): Performs a map operation which prepends "Hello " to each item in the stream
				.collect(Collectors.toList()): Defines a new collector which will collect the results of the pipeline 
				into a list and invokes collect to collect the results into it (and eventually returning the collected list).
		 * 
		 * 
		 * */
		
	}
	
	public static void intStream(){
		/* some of the methods (for a full listing see the javadocs) are

		Streams.intRange(start, stop, step) will generate a stream of primitive ints. (step if not provided is 1)
		Streams.longRange(start, stop, step) will generate a stream of primitive longs. (step if not provided is 1)
		Streams.concat(stream1, stream2) will concatenate two streams
		 */
		
		IntStream str1 = IntStream.range(1,11);
		IntStream str2 = IntStream.range(21,31);
		
		Stream<Integer> joined = Stream.concat(str1.boxed(), str2.boxed());
		
		System.out.println(joined.collect(Collectors.toList()));
	}
}
