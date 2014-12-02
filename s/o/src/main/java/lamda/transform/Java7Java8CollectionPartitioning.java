package lamda.transform;

import static java.util.stream.Collectors.partitioningBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class Java7Java8CollectionPartitioning {

	public static void main(String[] args) {
		Java7Java8CollectionPartitioning.partitionAdultsShouldSeparateKidsFromAdults();

	}

	
	
	 public static Map<Boolean, List<Person>> partitionAdults7(List<Person> people) {
	        Map<Boolean, List<Person>> map = new HashMap<>();
	        map.put(true, new ArrayList<>());
	        map.put(false, new ArrayList<>());
	        for (Person person : people) {
	            map.get(person.getAge() >= 18).add(person);
	        }
	        return map;
	    }
	 
	    public static Map<Boolean, List<Person>> partitionAdults(List<Person> people) {
	        return people.stream() // Convert collection to Stream
	                .collect(partitioningBy(p -> p.getAge() >= 18)); // Partition stream of people into adults (age => 18) and kids
	    }
	    
	    
	    public static void partitionAdultsShouldSeparateKidsFromAdults() {
	        Person sara = new Person("Sara", 4);
	        Person viktor = new Person("Viktor", 40);
	        Person eva = new Person("Eva", 42);
	        
	        List<Person> collection = asList(sara, eva, viktor);
	        Map<Boolean, List<Person>> result = partitionAdults(collection);
	        
	        System.out.println( result.get(true).equals( asList(viktor, eva)) ) ;
	        System.out.println( result.get(false).equals(asList(sara)) );
	        
//	        assertThat(result.get(true)).hasSameElementsAs(asList(viktor, eva));
//	        assertThat(result.get(false)).hasSameElementsAs(asList(sara));
//	        
	    }
	    
}
