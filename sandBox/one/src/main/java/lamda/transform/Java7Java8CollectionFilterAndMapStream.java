package lamda.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static java.util.Arrays.asList;

import static java.util.stream.Collectors.toSet;


public class Java7Java8CollectionFilterAndMapStream {

	public static void main(String[] args) {
		Java7Java8CollectionFilterAndMapStream.getKidNameShouldReturnNamesOfAllKidsFromNorway();
		
	}

	 public static Set<String> getKidNames7(List<Person> people) {
	        Set<String> kids = new HashSet<>();
	        for (Person person : people) {
	            if (person.getAge() < 18) {
	                kids.add(person.getName());
	            }
	        }
	        return kids;
	    }
	 
	    public static Set<String> getKidNames(List<Person> people) {
	        return people.stream()
	                .filter(person -> person.getAge() < 18) // Filter kids (under age of 18)
	                .map(Person::getName) // Map Person elements to names
	                .collect(toSet()); // Collect values to a Set
	    }
	
	  public static void getKidNameShouldReturnNamesOfAllKidsFromNorway() {
	        Person sara = new Person("Sara", 4);
	        Person viktor = new Person("Viktor", 40);
	        Person eva = new Person("Eva", 42);
	        Person anna = new Person("Anna", 5);
	        
	        List<Person> collection = asList(sara, eva, viktor, anna);
	        List<Person> expected = asList(sara,   anna);
	        
	        
	        System.out.println( getKidNames(collection).containsAll(expected) );
	        
	        //.  .contains("Sara", "Anna").doesNotContain("Viktor", "Eva");
	        
	        //assertThat(getKidNames(collection)).contains("Sara", "Anna").doesNotContain("Viktor", "Eva");
	        
	        
	    }
}
