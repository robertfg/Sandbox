package lamda.transform;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Java7Java8CollectionGrouping {

	public static void main(String[] args) {
		Java7Java8CollectionGrouping.partitionGroupByCountry();

	}
	
	
	
	

    public static Map<String, List<Person>> groupByNationality7(List<Person> people) {
        Map<String, List<Person>> map = new HashMap<>();
        for (Person person : people) {
            if (!map.containsKey(person.getNationality())) {
                map.put(person.getNationality(), new ArrayList<>());
            }
            map.get(person.getNationality()).add(person);
        }
        return map;
    }
 
    public static Map<String, List<Person>> groupByNationality(List<Person> people) {
        return people.stream() // Convert collection to Stream
                .collect(groupingBy(Person::getNationality)); // Group people by nationality
    }
    
    
	public static void partitionGroupByCountry() {
        Person sara = new Person("Sara", 4, "Norwegian");
        Person viktor = new Person("Viktor", 40, "Serbian");
        Person eva = new Person("Eva", 42, "Norwegian");
        List<Person> collection = asList(sara, eva, viktor);
        Map<String, List<Person>> result = groupByNationality(collection);
        
        System.out.println( result.get("Norwegian").containsAll( asList(sara, eva)  ) );
        System.out.println( result.get("Serbian").containsAll( asList(viktor)) );
         
        
        //assertThat(result.get("Norwegian")).hasSameElementsAs(asList(sara, eva));
        //assertThat(result.get("Serbian")).hasSameElementsAs(asList(viktor));
    }

}
