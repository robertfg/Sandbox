package lamda.transform;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

import static java.util.Arrays.asList;

public class Java7Java8CollectionStreamFlat {

	public static void main(String[] args) {
		Java7Java8CollectionStreamFlat.transformShouldFlattenCollection();

	}
	
	
	
	public static List<String> transform7(List<List<String>> collection) {
        List<String> newCollection = new ArrayList<>();
        for (List<String> subCollection : collection) {
            for (String value : subCollection) {
                newCollection.add(value);
            }
        }
        return newCollection;
    }
 
    public static List<String> transform(List<List<String>> collection) {
        return collection.stream() // Convert collection to Stream
                .flatMap(value -> value.stream()) // Replace list with stream
                .collect(toList()); // Collect results to a new list
    }
    
    
    public static void transformShouldFlattenCollection() {
        List<List<String>> collection = asList(asList("Viktor", "Farcic"), asList("John", "Doe", "Third"));
        List<String> expected = asList("Viktor", "Farcic", "John", "Doe", "Third");
       
        System.out.println( transform(collection).equals(expected) );
        
        System.out.println(transform(collection));
        System.out.println(expected);
        
        
    }
}
