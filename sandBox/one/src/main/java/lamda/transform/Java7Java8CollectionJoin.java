package lamda.transform;
import static java.util.stream.Collectors.joining;
import static java.util.Arrays.asList;


import java.util.List;

public class Java7Java8CollectionJoin {

	public static void main(String[] args) {
		Java7Java8CollectionJoin.toStringShouldReturnPeopleNamesSeparatedByComma();

	}

	
	
	 public static String namesToString7(List<Person> people) {
	        String label = "Names: ";
	        StringBuilder sb = new StringBuilder(label);
	        for (Person person : people) {
	            if (sb.length() > label.length()) {
	                sb.append(", ");
	            }
	            sb.append(person.getName());
	        }
	        sb.append(".");
	        return sb.toString();
	    }
	 
	    public static String namesToString(List<Person> people) {
	        return people.stream() // Convert collection to Stream
	                .map(Person::getName) // Map Person to name
	                .collect(joining(", ", "Names: ", ".")); // Join names
	    }
	    
	public static void toStringShouldReturnPeopleNamesSeparatedByComma() {
        Person sara = new Person("Sara", 4);
        Person viktor = new Person("Viktor", 40);
        Person eva = new Person("Eva", 42);
        List<Person> collection = asList(sara, viktor, eva);
          
        System.out.println(namesToString(collection).equals("Names: Sara, Viktor, Eva."));
    }
}
