package lamda;

import java.util.function.Supplier;

public class ReferencesToConstructors {

	    public static void main(String[] args) {
	      Supplier<ReferencesToConstructors> supplier = ReferencesToConstructors::new;
	      System.out.println(supplier.get());
	   }

}
