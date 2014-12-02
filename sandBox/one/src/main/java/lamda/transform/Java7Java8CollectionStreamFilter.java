package lamda.transform;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

public class Java7Java8CollectionStreamFilter {

	
	
	
	public static void main(String[] args) {
	    
		Java7Java8CollectionStreamFilter.transformShouldFilterCollection();

	}
	
	
	    public static List<String> transform7(List<String> collection) {
	        List<String> newCollection = new ArrayList<>();
	        for (String element : collection) {
	            if (element.length() < 4) {
	                newCollection.add(element);
	            }
	        }
	        return newCollection;
	    }
	 
	    public static List<String> transform(List<String> collection) {
	        return collection.stream() // Convert collection to Stream
	                .filter(value -> value.length() < 4) // Filter elements with length smaller than 4 characters
	                .collect(toList()); // Collect results to a new list
	    }
	    
	    public static void transformShouldFilterCollection() {
	        List<String> collection = asList("My", "name", "is", "John", "Doe");
	        List<String> expected = asList("My", "is", "Doe");
	        
	        
	        
	        System.out.println(transform(collection));
	        System.out.println(expected);
	        System.out.println( transform(collection).equals(expected) );
	        
	    }

}
