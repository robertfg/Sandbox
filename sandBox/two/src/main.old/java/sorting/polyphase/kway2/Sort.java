package sorting.polyphase.kway2;
/*
 * Modified by Brandon Chalk

 * TCSS 343
 * 4/15/10
 * 
 */
import java.util.*;

/**
 * A class that contains a group of sorting algorithms.
 * The input to the sorting algorithms is assumed to be
 * an array of integers.
 * 
 * @author Donald Chinn
 * @version September 19, 2003
 */
public class Sort {

    // Constructor for objects of class Sort
    public Sort() {
    }


    /**
     * Given an array of integers and an integer k, sort the array
     * (ascending order) using k-way mergesort.
     * @param data  an array of integers
     * @param k     the k in k-way mergesort
     */
    public static void kwayMergesort (int[] data, int k) {
        kwayMergesortRecursive (data, 0, data.length - 1, k);
    }
    
    /**
     * The recursive part of k-way mergesort.
     * Given an array of integers (data), a low index, high index, and an integer k,
     * sort the subarray data[low..high] (ascending order) using k-way mergesort.
     * @param data  an array of integers
     * @param low   low index
     * @param high  high index
     * @param k     the k in k-way mergesort
     */
    public static void kwayMergesortRecursive (int[] data, int low, int high, int k) {
        if (low < high) {
            for (int i = 0; i < k; i++) {
                kwayMergesortRecursive (data,
                                        low + i*(high-low+1)/k,
                                        low + (i+1)*(high-low+1)/k - 1,
                                        k);
            }
            merge (data, low, high, k);
        }
    }
    

    /**
     * Given an array of integers (data), a low index, a high index, and an integer k,
     * sort the subarray data[low..high].  This method assumes that each of the
     * k subarrays  data[low + i*(high-low+1)/k .. low + (i+1)*(high-low+1)/k - 1],
     * for i = 0..k-1, are sorted.
     */
    public static void merge (int[] data, int low, int high, int k) {
    
        if (high < low + k) {
            // the subarray has k or fewer elements
            // just make one big heap and do deleteMins on it
            Comparable[] subarray = new MergesortHeapNode[high - low + 1];
            for (int i = 0, j = low; i < subarray.length; i++, j++) {
                subarray[i] = new MergesortHeapNode(data[j], 0);
            }
            BinaryHeap heap = BinaryHeap.buildHeap(subarray);
            for (int j = low; j <= high; j++) {
                try {
                    data[j] = ((MergesortHeapNode) heap.deleteMin()).getKey();
                }
                catch (EmptyHeapException e) {
                    System.out.println ("Tried to delete from an empty heap.");
                }
            }
            
        } else {
            // divide the array into k subarrays and do a k-way merge
        	final int subarrSize = high-low+1;
        	final int[] tempArray = new int[subarrSize];
        	
        	// Make temp array
        	for (int i = low; i < high + 1; i++)
        		tempArray[i-low] = data[i];
        	
        	// Keep subarray index to keep track of where we are in each subarray
        	final int[] subarrayIndex = new int[k];
        	for (int i = 0; i < k; i++)
        		subarrayIndex[i] = i*(subarrSize)/k;
        	
        	// Build heap
        	Comparable[] subarray = new MergesortHeapNode[k];
    		for (int i = 0; i < k; i++)
                subarray[i] = new MergesortHeapNode(tempArray[subarrayIndex[i]++], i);

            BinaryHeap heap = BinaryHeap.buildHeap(subarray);
        	
        	// For each element low to high, find the lowest in each k subarray
        	for (int i = low; i < high + 1; i++)
        	{
        		
        		// Take lowest element and add back in to original array
    			try
				{
    				MergesortHeapNode a = ((MergesortHeapNode) heap.deleteMin());
					data[i] = a.getKey();
    				if (subarrayIndex[a.getWhichSubarray()] < (a.getWhichSubarray()+1)*(subarrSize)/k)
    				{
    					heap.insert(new MergesortHeapNode(tempArray[subarrayIndex[a.getWhichSubarray()]]++, a.getWhichSubarray()));
    					
    	    			// Increment the subarray index where the lowest element resides
    	    			subarrayIndex[a.getWhichSubarray()]++;
    				}
				} catch (EmptyHeapException e)
				{
                    System.out.println ("Tried to delete from an empty heap.");
				}
        	}
        }
    }
    
    
    /**
     * Given an integer size, produce an array of size random integers.
     * The integers of the array are between 0 and size (inclusive) with
     * random uniform distribution.
     * @param size  the number of elements in the returned array
     * @return      an array of integers
     */
    public static int[] getRandomArrayOfIntegers(int size) {
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = (int) ((size + 1) * Math.random());
        }
        return data;
    }
    

    /**
     * Given an integer size, produce an array of size random integers.
     * The integers of the output array are between 0 and size-1 with
     * exactly one of each in the array.  Each permutation is generated
     * with random uniform distribution.
     * @param size  the number of elements in the returned array
     * @return      an array of integers
     */
    public static int[] getRandomPermutationOfIntegers(int size) {
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = i;
        }
        // shuffle the array
        for (int i = 0; i < size; i++) {
            int temp;
            int swap = i + (int) ((size - i) * Math.random());
            temp = data[i];
            data[i] = data[swap];
            data[swap] = temp;
        }
        return data;
    }


    /**
     * Perform checks to see if the algorithm has a bug.
     */
    private static void testCorrectness() {
        int[] data = getRandomPermutationOfIntegers(100);
        
        for (int i = 0; i < data.length; i++) {
            System.out.println("data[" + i + "] = " + data[i]);
        }
        
        int k = 4;
        kwayMergesort(data, k);
        
        // verify that data[i] = i
        for (int i = 0; i < data.length; i++) {
            if (data[i] != i) {
                System.out.println ("Error!  data[" + i + "] = " + data[i] + ".");
            }
        }
    }
    
    
    /**
     * Perform timing experiments.
     */
    private static void testTiming () {
        // timer variables
        long totalTime = 0;
        long startTime = 0;
        long finishTime = 0;

        //n = 200000; 400000; 800000; 1600000; and 3200000
        int[] n = {200000, 400000, 800000, 1600000, 3200000};    // n = size of the array
        
        //k = 2; 3; 5; 10; 20; 50
        int[] k = {2, 3, 5, 10, 20, 50};         // k = k in k-way mergesort

    	for (int j = 0; j < k.length; j++)
        {
            for (int i = 0; i < n.length; i++)
        	{
        		System.out.print(n[i] + "\t" + k[j] + "\t");
                for (int trial = 0; trial < 3; trial++)
                {
                    // start the timer
                    Date startDate = new Date();
                    startTime = startDate.getTime();
                    
	                int[] data = getRandomArrayOfIntegers(n[i]);
	                kwayMergesort(data, k[j]);
	
	                // stop the timer
	                Date finishDate = new Date();
	                finishTime = finishDate.getTime();
	                totalTime = (finishTime - startTime);
	                
	                System.out.print(totalTime + "\t");
	                
	               /* System.out.println("** Results for k-way mergesort:");
	                System.out.println("    " + "n = " + n + "    " + "k = " + k);
	                System.out.println("    " + "Time: " + totalTime + " ms.");*/
	        	}
                System.out.println();
	        }
        }
    }
    
    
    /**
     * code to test the sorting algorithms
     */
    public static void main (String[] argv) {
        testCorrectness();
        testTiming();
    }
}
