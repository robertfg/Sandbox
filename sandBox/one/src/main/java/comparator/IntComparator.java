package comparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IntComparator implements Comparator<Integer> {

	@Override
	public int compare(Integer o1, Integer o2) {
		// TODO Auto-generated method stub
		return 0;
	}  
 
	
	  
		public static void main(String[] args){
			System.out.println(new Integer("-10"));
			System.out.println(new Integer("+10"));
		  //System.out.println("Christopher".substring(0,"Christopher".indexOf(".")));
			
			List<Integer> test = new ArrayList<Integer>();
			test.add(1);
			test.add(12);
			test.add(13);
			test.add(14);
			test.add(0);
			test.add(-1);
			test.add(-12);
			test.add(-13);
			test.add(-14);
			
			
			Collections.sort(test, new Comparator<Integer>() {
			

				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}  
			});
			
			for (Integer integer : test) {
			System.out.println(integer);	
			}
			
		}
		
}
