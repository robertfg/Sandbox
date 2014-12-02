package lamda;

import java.util.Arrays;
import java.util.List;


//http://javarevisited.blogspot.sg/2014/02/10-example-of-lambda-expressions-in-java8.html
public class LamdaFeatures {

	public static void main(String[] args){
		
		LamdaFeatures.iteration();
		LamdaFeatures.creatingThread();
		
	}
	
	
	/*
	 
	Syntax of lambda expression in Java 8. You can write following kind of code using lambdas :
	(params) -> expression
	(params) -> statement
	(params) -> { statements }
	
	Read more: http://javarevisited.blogspot.com/2014/02/10-example-of-lambda-expressions-in-java8.html#ixzz3Gwsdve9Y 
	 */
	
	
	public static void iteration(){
		//Read more: http://javarevisited.blogspot.com/2014/02/10-example-of-lambda-expressions-in-java8.html#ixzz3Gwq3Pdde
		List features = Arrays.asList("Lambdas", "Default Method", "Stream API", "Date and Time API");
		

		features.forEach(x -> System.out.println(x));
		
		// Even better use Method reference feature of Java 8
		// method reference is denoted by :: (double colon) operator 
		// looks similar to score resolution operator of C++
		
		
		
		System.out.println("\nUsing another approach\n");
		
		
		features.forEach(System.out::println); 

		
	}
	
	
	public static void creatingThread(){
		
		
		new Thread( () -> System.out.println("In Java8, Lambda expression rocks !!") ).start();

		//Read more: http://javarevisited.blogspot.com/2014/02/10-example-of-lambda-expressions-in-java8.html#ixzz3GwrKg0Ps
	}
}
