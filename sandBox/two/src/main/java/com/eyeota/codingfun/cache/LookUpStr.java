package com.eyeota.codingfun.cache;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import sun.misc.Unsafe;

public class LookUpStr implements OffHeapStr {

	private AtomicInteger seed = new AtomicInteger(-1);

	private static int FIELD_SIZE;
	private final static int index_offset = (FIELD_SIZE = 0);
	
	private final static int value_offset = (FIELD_SIZE += 4);
    
	private final static int  VALUE_SIZE_LIMIT = 50; //50 character
	private final static byte VALUE_DELIMETER  = '\0'; 
	
	private final static byte[] BLANK  = "���".getBytes(); 

	private static int PROJECTION_SIZE;
	
	private static int OBJECT_SIZE = (FIELD_SIZE + VALUE_SIZE_LIMIT );
	
	private long ADDRESS_POINTER;
	
	private long OBJECT_POINTER;
	
	private static Unsafe unsafe;
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (Exception e) {			
		}
	}
	
	public LookUpStr(int projection) {
		PROJECTION_SIZE = projection;
		ADDRESS_POINTER=unsafe.allocateMemory(OBJECT_SIZE * PROJECTION_SIZE);
		
	}
	
	
	@Override
	public void setIndex(int index) {
		unsafe.putInt(OBJECT_POINTER + index_offset, index);
	}

	@Override
	public int getIndex() {
		return unsafe.getInt(OBJECT_POINTER + index_offset);	
	}
	
	@Override
	public void setValue(byte[] value) {
	 checkifSpace(value);
	  int i = 0;
	  for (; i < value.length; i++) {
		 unsafe.putByte(OBJECT_POINTER + value_offset + i , value[i]);
	   }
	
	   unsafe.putByte(OBJECT_POINTER + value_offset + value.length , VALUE_DELIMETER );
	}
	
	/**
	 *  
	 */
	@Override
	public byte[] getValue(int index) {
		long objOffset =  ADDRESS_POINTER + (index*OBJECT_SIZE * 1L);
		byte[] value = new byte[VALUE_SIZE_LIMIT];
		for (int i = 0; i < value.length; i++) {
			value[i] = unsafe.getByte( objOffset + value_offset + i);
		
			if(value[i]==VALUE_DELIMETER){
	    		byte[] trim = new byte[i];
	    		for (int ctr = 0; ctr < trim.length; ctr++) {
					trim[ctr] = value[ctr];
				}
	    		value = null;
	    		if( equals(trim,(BLANK))){ 
	    			return new byte[]{};
	    		 }
	    		
	    		return trim;
			}
		}
	   return value;
		
	}

    
	@Override
	public void moveTo(int index) {
		OBJECT_POINTER = ADDRESS_POINTER + (index*OBJECT_SIZE * 1L);
	}


	@Override
	public boolean exist(byte[] value) {
		value = checkifSpace(value);
		for(int index = 0; index< PROJECTION_SIZE; index++){
			if(equals(value,getvalue(index)) ){
				return true;
			}
		}
		return false;
	}
	
	
	
	private boolean equals(byte[] a, byte[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }

	/**
	 * get index given actual string value as filter
	 */
	@Override
	public int getIndex(byte[] value) {
		value = checkifSpace(value);
		
		if(seed.get() <= -1){
			return -1;	
		} else {
			for(int index = 0; index< PROJECTION_SIZE; index++){
				if(equals(value,getvalue(index)) ) {
					return unsafe.getInt(ADDRESS_POINTER + (index*OBJECT_SIZE * 1L) + index_offset);
				}
			}
		}
		return -1;
		
	}
	
	
	/**
	 * return string index code
	 */
	public int create(byte[] value){
		value = checkifSpace(value);
		seed.getAndIncrement();
		unsafe.putInt(  (ADDRESS_POINTER  + (seed.get() *OBJECT_SIZE * 1L))  + index_offset, seed.get());
	    setValue(value, (ADDRESS_POINTER  + (seed.get() *OBJECT_SIZE * 1L)));	
	    return seed.get();
	}
	
	
	public void setValue(byte[] value, long memAddress) {
		value = checkifSpace(value);	
	   for (int i = 0; i < value.length; i++) {
		 unsafe.putByte(memAddress + value_offset + i , value[i]);
	   }
	   unsafe.putByte(memAddress + value_offset + value.length , VALUE_DELIMETER );
	}
	
	private byte[] checkifSpace(byte[] value){
		if(value.length==0){
		   value = BLANK;
		}
		return value;
	}
	
	public byte[] getvalue(int index) {
		long objOffset =  ADDRESS_POINTER + (index*OBJECT_SIZE * 1L);
		byte[] value = new byte[VALUE_SIZE_LIMIT];
		for (int i = 0; i < value.length; i++) {
			value[i] = unsafe.getByte( objOffset + value_offset + i);
		
			if(value[i]==VALUE_DELIMETER){
	    		byte[] trim = new byte[i];
	    		for (int ctr = 0; ctr < trim.length; ctr++) {
					trim[ctr] = value[ctr];
				}
	    		value = null;
	    		return trim;
			}
		}
	   return value;
		
	}
	
}
