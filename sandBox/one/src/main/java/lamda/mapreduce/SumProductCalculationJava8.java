package lamda.mapreduce;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

//http://deors.wordpress.com/2014/07/04/java-8-map-reduce/
public class SumProductCalculationJava8 {
	public static void main(String[] args) {
		List<Pair<Integer, Integer>> thePairList = new ArrayList<>();
		thePairList.add(new Pair<Integer, Integer>(10, 1));
		thePairList.add(new Pair<Integer, Integer>(12, 2));
		thePairList.add(new Pair<Integer, Integer>(14, 3));
		thePairList.add(new Pair<Integer, Integer>(16, 4));
		thePairList.add(new Pair<Integer, Integer>(18, 5));
		thePairList.add(new Pair<Integer, Integer>(20, 6));
		thePairList.add(new Pair<Integer, Integer>(22, 7));
		thePairList.add(new Pair<Integer, Integer>(24, 8));
		thePairList.add(new Pair<Integer, Integer>(26, 9));
		thePairList.add(new Pair<Integer, Integer>(28, 10));
		thePairList.add(new Pair<Integer, Integer>(30, 11));
		thePairList.add(new Pair<Integer, Integer>(32, 12));
		thePairList.add(new Pair<Integer, Integer>(34, 13));
		thePairList.add(new Pair<Integer, Integer>(36, 14));

		Integer result = thePairList.parallelStream()
				.mapToInt(p -> p.getValue0() * p.getValue1()).sum();
		
		System.out.printf("the final result is %s\n", result);
		
	}
}