package lamda.transform;

import java.util.List;
import static java.util.Arrays.asList;


public class Java7Java8CollectionSumAndCalculate {

	public static void main(String[] args) {
		
		Java7Java8CollectionSumAndCalculate.transformShouldConvertCollectionElementsToUpperCase();
	}

	
	
	public static int calculate7(List<Integer> numbers) {
        int total = 0;
        for (int number : numbers) {
            total += number;
        }
        return total;
    }
 
    public static int calculate(List<Integer> people) {
        return people.stream() // Convert collection to Stream
                .reduce(0, (total, number) -> total + number); // Sum elements with 0 as starting value
    }
    
    public static void transformShouldConvertCollectionElementsToUpperCase() {
        List<Integer> numbers = asList(1, 2, 3, 4, 5);
          System.out.println( calculate(numbers) ==    (1 + 2 + 3 + 4 + 5) );
        
    }
}
