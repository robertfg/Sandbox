package lamda.collection;

import java.util.Arrays;
import java.util.List;

public class MapReduceTest2 {

	  public static void main(String[] args)
	  {
	    List<String> al = Arrays.asList( new String[] { 
	      "This sample is by Steve from doublecloud.org, a leading ", 
	      "technical blog on virtualization, cloud computing, and ",
	      "software architecture." });
	 
	    int total = al.parallelStream().mapToInt(e -> e.split(" ").length).sum();
	    System.out.println("Total words:" + total);
	  }
	}