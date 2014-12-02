package array;

import java.lang.reflect.Array;


public class EnlargeArray<T> { 
	
	public static void main(String[] args) { 
		Integer[] myArray = { 1, 2, 3, 4 }; 
		// Show original
		System.out.println(java.util.Arrays.toString(myArray)); 
		// Enlarge it twofold 
		myArray = EnlargeArray.doubleSize(myArray); 
		for(int i = 0;i < myArray.length;i++) { 
			myArray[i] = i + 1;
			} 
		System.out.println(java.util.Arrays.toString(myArray)); 
	
	} 
	
	public static <T> T[] doubleSize(T[] original) {
		
		T[] result = (T[])Array.newInstance(original[0].getClass(), original.length * 2);
		System.arraycopy(original, 0, result, 0, original.length); return result; 
		
	}
	
}
	


