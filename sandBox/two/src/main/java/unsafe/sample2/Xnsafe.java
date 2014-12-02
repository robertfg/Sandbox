package unsafe.sample2;

import java.lang.reflect.Field;

import sun.misc.Unsafe;


public final class Xnsafe {

	private static Unsafe unsafe;
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);			
		} catch (Exception e) {			
		} 
	}
	
}