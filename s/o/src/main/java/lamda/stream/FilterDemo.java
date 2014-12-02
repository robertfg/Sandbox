package lamda.stream;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FilterDemo {

	
	   public static void main(String[] args) {
	        List<Employee> list = Employee.getEmpList();
	        
	        Predicate<Employee> filterPredicate = e -> e.id > 1 && e.sal < 6000;
	        
	        Consumer<Employee> printConsumer = e -> System.out.println(e.id + ", "+e.sal);
	        
	        list.stream().filter(filterPredicate).forEach(printConsumer);
	    
	   }
	   
	   
}
