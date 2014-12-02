package lamda.stream;

import java.util.List;
import java.util.function.Predicate;


public class MatchDemo {
	  public static void main(String[] args) {
	     Predicate<Employee> p1 = e -> e.id < 10 && e.name.startsWith("A");
	     Predicate<Employee> p2 = e -> e.sal < 10000;
	     List<Employee> list = Employee.getEmpList();
	     //using allMatch
	     boolean b1 = list.stream().allMatch(p1);
	     System.out.println(b1);
	     boolean b2 = list.stream().allMatch(p2);
	     System.out.println(b2);
	     //using anyMatch
	     boolean b3 = list.stream().anyMatch(p1);
	     System.out.println(b3);
	     boolean b4 = list.stream().anyMatch(p2);
	     System.out.println(b4);
	     //using noneMatch
	     boolean b5 = list.stream().noneMatch(p1);
	     System.out.println(b5);
	     
	  }    
	} 
