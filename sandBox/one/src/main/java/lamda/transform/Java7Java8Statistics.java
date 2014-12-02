package lamda.transform;

import static java.util.Arrays.asList;

import java.util.IntSummaryStatistics;
import java.util.List;

public class Java7Java8Statistics {

	public static void main(String[] args) {
		Java7Java8Statistics.getStatsShouldReturnAverageAge();
		Java7Java8Statistics.getStatsShouldReturnMaximumAge();
		Java7Java8Statistics.getStatsShouldReturnNumberOfPeople();
		Java7Java8Statistics.getStatsShouldReturnSumOfAllAges();

	}

	static Person sara = new Person("Sara", 4);
	static Person viktor = new Person("Viktor", 40);
	static Person eva = new Person("Eva", 42);
    static List<Person> collection = asList(sara, eva, viktor);
 
    public static void getStatsShouldReturnAverageAge() {
       System.out.println( getStats(collection).getAverage() ==  (double)(4 + 40 + 42) / 3); 
                
    }
 
   
    public static void getStatsShouldReturnNumberOfPeople() {
    	System.out.println(getStats(collection).getCount() == 3);
    }
 
   
    public static void getStatsShouldReturnMaximumAge() {
    	System.out.println( getStats(collection).getMax() == 42);
    }
 
    
    public static void getStatsShouldReturnMinimumAge() {
    	System.out.println( getStats(collection).getMin() == 4);
    }
 
 
    public static void getStatsShouldReturnSumOfAllAges() {
    	System.out.println( getStats(collection).getSum() == (40 + 42 + 4) );
    }
	 public static Stats getStats7(List<Person> people) {
	        long sum = 0;
	        int min = people.get(0).getAge();
	        int max = 0;
	        for (Person person : people) {
	            int age = person.getAge();
	            sum += age;
	            min = Math.min(min, age);
	            max = Math.max(max, age);
	        }
	        return new Stats(people.size(), sum, min, max);
	    }
	 
	    public static IntSummaryStatistics getStats(List<Person> people) {
	        return people.stream()
	                .mapToInt(Person::getAge)
	                .summaryStatistics();
	    }
}
