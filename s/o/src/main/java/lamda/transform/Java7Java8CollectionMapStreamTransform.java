package lamda.transform;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

public class Java7Java8CollectionMapStreamTransform {

	public static void main(String[] args) {
		
		Java7Java8CollectionMapStreamTransform.transformShouldConvertCollectionElementsToUpperCase();
	}

	
	 public static List<String> transform7(List<String> collection) {
	        List<String> coll = new ArrayList<>();
	        for (String element : collection) {
	            coll.add(element.toUpperCase());
	        }
	        return coll;
	    }
	 
	    public static List<String> transform(List<String> collection) {
	        return collection.stream() // Convert collection to Stream
	                .map(String::toUpperCase) // Convert each element to upper case
	                .collect(toList()); // Collect results to a new list
	    }


	    public static void transformShouldConvertCollectionElementsToUpperCase() {
	        List<String> collection = asList("My", "name", "is", "John", "Doe");
	        List<String> expected = asList("MY", "NAME", "IS", "JOHN", "DOE");
	       // assertThat().hasSameElementsAs(expected);
	        
	        System.out.println( transform(collection).equals(expected) );
	        
	        System.out.println(transform(collection));
	        System.out.println(expected);
	        
	        
	    }
}

