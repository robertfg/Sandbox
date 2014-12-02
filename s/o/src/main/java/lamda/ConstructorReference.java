package lamda;



	class Employee
	{
	   String name;
	   Integer age;

	   Employee()
	   {
	      name = "unknown";
	      age = 100;
	   }

	   Employee(String name, Integer age)
	   {
	      this.name = name;
	      this.age = age;
	   }
	}

	@FunctionalInterface
	interface EmployeeProvider
	{
	   Employee getEmployee(String name, Integer age);
	}

	public class ConstructorReference {
	
	   public static void main(String[] args)
	   {
	      EmployeeProvider provider = Employee::new;
	      
	      Employee emp = provider.getEmployee("John Doe", 47);
	     
	      System.out.printf("Name: %s%n", emp.name);
	      System.out.printf("Age: %d%n", emp.age);
	   
	   }
	}