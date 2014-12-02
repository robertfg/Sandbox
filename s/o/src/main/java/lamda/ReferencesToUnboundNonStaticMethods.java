package lamda;

import java.util.function.Function;

public class ReferencesToUnboundNonStaticMethods {

	public static void main(String[] args)
	   {
	      print(String::toString, "some string to be printed");
	      print(s -> s.toString(), "some string to be printed");
	      print(new Function<String, String>()
	      {
	         @Override
	         public String apply(String s) // receives argument in parameter
	         {                             // and doesn't need to close over
	            return s.toString();       // it
	         }
	      }, "some string to be printed");
	   }

	   public static void print(Function<String, String> function, String value)
	   {
	      System.out.println(function.apply(value));
	   }
}
