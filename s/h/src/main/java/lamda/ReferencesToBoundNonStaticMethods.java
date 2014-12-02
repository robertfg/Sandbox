package lamda;

import java.util.function.Supplier;

public class ReferencesToBoundNonStaticMethods {

	 public static void main(String[] args)
	   {
	      String s = "method references are cool";
	      
	      print(s::toString);
	      
	      print(() -> s.toString());
	      
	      print(new Supplier<String>()
	      {
	         @Override
	         public String get()
	         {
	            return s.toString(); // closes over s
	         }
	      });
	      
	   }

	   public static void print(Supplier<String> supplier)
	   {
	      System.out.println(supplier.get());
	   }
	   
}
