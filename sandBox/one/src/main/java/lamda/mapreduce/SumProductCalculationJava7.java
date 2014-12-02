package lamda.mapreduce;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.javatuples.Pair;

public class SumProductCalculationJava7 extends RecursiveTask<Integer> {
	private static final long serialVersionUID = 6939566748704874245L;
	private int threshold = 10;
	List<Pair<Integer, Integer>> pairList;

	public SumProductCalculationJava7(List<Pair<Integer, Integer>> pairList) {
		super();
		this.pairList = pairList;
	}

	@Override
	protected Integer compute() {
		System.out.printf("fragment %s size %d\n", this, pairList.size());
		if (pairList.size() <= threshold) {
			return computeDirect();
		}
		int split = pairList.size() / 2;

		List<Pair<Integer, Integer>> forkedList1 = pairList.subList(0, split);
		
		SumProductCalculationJava7 forkedTask1 = new SumProductCalculationJava7(
				forkedList1);
		forkedTask1.fork();

		List<Pair<Integer, Integer>> forkedList2 = pairList.subList(split,
				pairList.size());
		SumProductCalculationJava7 forkedTask2 = new SumProductCalculationJava7(
				forkedList2);
		forkedTask2.fork();

		return forkedTask1.join() + forkedTask2.join();
	}

	private Integer computeDirect() {
		Integer sumproduct = 0;
		for (Pair<Integer, Integer> pair : pairList) {
			sumproduct += pair.getValue0() * pair.getValue1();
		}
		System.out.printf("fragment %s total %s\n", this, sumproduct);
		return sumproduct;
	}


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

		    SumProductCalculationJava7 theTask = new SumProductCalculationJava7(thePairList);
		    ForkJoinPool thePool = new ForkJoinPool();
		    Integer result = thePool.invoke(theTask);
		    System.out.printf("the final result is %s\n", result);
		  }
}