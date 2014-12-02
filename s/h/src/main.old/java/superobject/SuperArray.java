package superobject;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

class SuperArray {
	private static Unsafe unsafe;
	
	public static Unsafe getUnsafe() {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);			
			return unsafe;
		} catch (Exception e) {			
		} 
		return null;
	}
	
    private final static int BYTE = 1;

    private long size;
    private long address;

    public SuperArray(long size) {
        this.size = size;
        address = getUnsafe().allocateMemory(size * BYTE);
    }

    public void set(long i, byte value) {
        getUnsafe().putByte(address + i * BYTE, value);
    }

    public int get(long idx) {
        return getUnsafe().getByte(address + idx * BYTE);
    }

    public long size() {
        return size;
    }
    
    
    public static void main(String args[]){
    	
    	long SUPER_SIZE = (long)Integer.MAX_VALUE * 2;
    	
    	SuperArray array = new SuperArray(SUPER_SIZE);
    	System.out.println("Array size:" + array.size()); // 4 294 967 294
    	int sum =0;
    	for (int i = 0; i < 10; i++) {
    	    
    		array.set( (long) Integer.MAX_VALUE + i , (byte)1000);
    	  
    	    sum += array.get((long)Integer.MAX_VALUE + i);
    	}
    	System.out.println("Sum of 100 elements:" + sum);  // 300
    }
}