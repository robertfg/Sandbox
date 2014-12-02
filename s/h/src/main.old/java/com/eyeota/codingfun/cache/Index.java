package com.eyeota.codingfun.cache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import sun.misc.Unsafe;

public class Index implements OffHeapInt {
	
	private AtomicInteger seed = new AtomicInteger(-1);
	
	private static int FIELD_SIZE;
	private final static int index_offset = (FIELD_SIZE = 0);

	private final static int key_offset   = (FIELD_SIZE += 4);
	private final static int value_offset = (FIELD_SIZE += 4);
	private final static int parent_offset = (FIELD_SIZE += 4);

	
	
	private static int PROJECTION_SIZE;
	private static int OBJECT_SIZE = (FIELD_SIZE += 6);
	
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
	
	public Index(int projection) {
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
	public int getValue() {
	 //   System.out.println("getValuePointer:" + (OBJECT_POINTER + value_offset));
		
		return unsafe.getInt(OBJECT_POINTER + value_offset);	
	}
	
	@Override
	public void setValue(int value) {
		unsafe.putInt(OBJECT_POINTER + value_offset, value);
	}

	@Override
	public void jump(int index) { // 
		if(index > PROJECTION_SIZE){
			System.out.println("Must implement");
		}else if(index > seed.get()){
			seed.set(index);
		}
//		System.out.println( "jumpPonter:     " + (ADDRESS_POINTER + (index * OBJECT_SIZE * 1L))  );
		OBJECT_POINTER = ADDRESS_POINTER + (index * OBJECT_SIZE * 1L);
//		System.out.println( "OBJECT_POINTER: " + OBJECT_POINTER );
		
	}
    
	@Override
	public int getValue(int index) {
		if(seed.get()==-1){
			return -1;
		} else{
			long objOffset =  ADDRESS_POINTER + (index*OBJECT_SIZE * 1L);
			return unsafe.getInt(objOffset + value_offset);	
		}
	}

	
	
    /**
     * will optimize the searching
     */
	@Override
	public boolean exist(int value) {  
		for(int index = 0; index < PROJECTION_SIZE; index++){
			if( value ==  getValue(index)  ) {
				return true;
			}
		}
		return false;
	}
	
	
	
	@Override
	public int getIndex(int value ) {
		for(int index = 0; index< PROJECTION_SIZE; index++){
			if( value == getValue(index) ){
				return unsafe.getInt(ADDRESS_POINTER + (index*OBJECT_SIZE * 1L) + index_offset);
			}
		}
		return -1;
	}
	
	public int create(int value){
		seed.getAndIncrement(); 
		unsafe.putInt( (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))  + index_offset, seed.get());
		unsafe.putInt( (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))  + value_offset, value);
	    return seed.get();
	}


	@Override
	public void setKey(int key) {
		unsafe.putInt(OBJECT_POINTER + key_offset, key);
	}


	@Override
	public int getKey() {
		return unsafe.getInt(OBJECT_POINTER + key_offset);	
	}


	@Override
	public int getKey(int index) {
		if(seed.get()==-1){
			return -1;
		} else{
			long objOffset =  ADDRESS_POINTER + (index*OBJECT_SIZE * 1L);
			return unsafe.getInt(objOffset + key_offset);	
		}
	}


	@Override
	public int create(int key, int value) {
		seed.getAndIncrement(); 
		System.out.println( "objectPointer:" + (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))   );

		unsafe.putInt( (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))  + index_offset, seed.get());
		System.out.println( "indexPointer: " + (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L)  + index_offset) );
 
		unsafe.putInt( (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))  + key_offset,   key);
		System.out.println( "keyPointer:   " + (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L)  + key_offset) );
	
		unsafe.putInt( (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))  + value_offset, value);
	   System.out.println( "valuePointer: " + (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L)  + value_offset) );

		
	    return seed.get();

	}

	@Override
	public int[] getKV(int index) {
		int[] kv = new int[2];
		if(seed.get()==-1){
			kv[0]=-1;
		} else{
			long objOffset =  ADDRESS_POINTER + (index*OBJECT_SIZE * 1L);
			kv[0]=unsafe.getInt(objOffset + key_offset);
			kv[1]=unsafe.getInt(objOffset + value_offset);
			return kv; 	
		}
		return kv;
	}


	@Override
	public int getV(int K) {
		for(int index = 0; index< PROJECTION_SIZE; index++){
			if( K == getKey(index) ){
				return unsafe.getInt(ADDRESS_POINTER + (index*OBJECT_SIZE * 1L) + value_offset);
			}
		}
		return -1;
	}
	
	@Override
	public int[] getVs(int K) {
		/*this should change to resizing primitive array*/
		List<Integer> vS = new ArrayList<Integer>();
		for(int index = 0; index< PROJECTION_SIZE; index++){
			if( K == getKey(index) ){
				vS.add( new Integer( unsafe.getInt(ADDRESS_POINTER + (index*OBJECT_SIZE * 1L) + value_offset)));
			} 
		}
		return toIntArray( vS );
	}
	
	@Override
	public int[] getKs(int V) {
		/*this should change to resizing primitive array*/
		List<Integer> kS = new ArrayList<Integer>();
		for(int index = 0; index< PROJECTION_SIZE; index++){
			if( V == getValue(index) ){
				kS.add( new Integer( unsafe.getInt(ADDRESS_POINTER + (index*OBJECT_SIZE * 1L) + key_offset)));
			} 
		}
		return toIntArray( kS );
	}


	@Override
	public int getK(int V) {
		for(int index = 0; index< PROJECTION_SIZE; index++){
			if( V == getValue(index) ){
				return unsafe.getInt(ADDRESS_POINTER + (index * OBJECT_SIZE * 1L) + key_offset);
			}
		}
		return -1;
	}


	@Override
	public boolean containsKey(int key) {
		for(int index = 0; index< PROJECTION_SIZE; index++){
			if( key == getKey(index) ){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(int value) {
		for(int index = 0; index< PROJECTION_SIZE; index++){
			if( value == getValue(index) ){
				return true;
			}
		}
		return false;
	}
	
	
	public void resizeArray(int minCapacity) {
	    
	  }
	
	private int[] toIntArray(List<Integer> list)  {
	    int[] ret = new int[list.size()];
	    int i = 0;
	    for (Integer e : list)  
	        ret[i++] = e.intValue();
	    return ret;
	}


	@Override
	public int[] getIndex(int key, int value) {
		List<Integer> iS = new ArrayList<Integer>(); 
		int[] index= getIndexK(key);
		 for (int i = 0; i < index.length; i++) {
		     if( value == getValue( index[i])) { //getvalue using index
		    	 iS.add( new Integer( index[i]) );
		     }
		 }
		 return toIntArray( iS );
	}

	@Override
	public int[] getIndexV(int value) {
		List<Integer> iV = new ArrayList<Integer>();
		for(int index = 0; index< PROJECTION_SIZE; index++){
			if( value == getValue(index) ){
				iV.add( new Integer( index) );
			}
		}
		return toIntArray( iV );
	}


	@Override
	public int[] getIndexK(int key) {
		List<Integer> iK = new ArrayList<Integer>();
		for(int index = 0; index< PROJECTION_SIZE; index++){
			if( key == getKey(index) ){
				iK.add( new Integer( index ));
			}
		}
		return toIntArray( iK );
	}


	@Override
	public boolean containsKeyValue(int key, int value) {
		int[] index =  getIndexK(key);
		if(index.length <=0){
			return false;
		} else {
			for (int i = 0; i < index.length; i++) {
				 if( value ==  getValue( index[i] ) ){
					 return true;
				 }
				
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unused")
	private int filter(int value, int values[]){
	    for (int i = 0; i < values.length; i++) {
			if(values[i]==value){
				return value;
			}
       }
        return -1;
	}


	@Override
	public void setParent(int parent) {
		unsafe.putInt(OBJECT_POINTER + parent_offset, parent);
	}


	@Override
	public int[] getIndex(int key, int value, int parent) {
		List<Integer> iS = new ArrayList<Integer>(); 
		int[] index= getIndexK(key);
		 for (int i = 0; i < index.length; i++) {
		     if( parent == getParent( index[i])) { //getvalue using index
		    	 iS.add( new Integer( index[i]) );
		     }
		 }
		 return toIntArray( iS );
	}


	@Override
	public int getParent() {
		return unsafe.getInt(OBJECT_POINTER + parent_offset);	
	}


	@Override
	public int getParent(int index) {
		if(seed.get()==-1){
			return -1;
		} else{
			long objOffset =  ADDRESS_POINTER + (index*OBJECT_SIZE * 1L);
			return unsafe.getInt(objOffset + parent_offset);	
		}
	}


	@Override
	public int create(int key, int value, int parent) {
		seed.getAndIncrement(); 
		//	System.out.println( "objectPointer:" + (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))   );

			unsafe.putInt( (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))  + index_offset, seed.get());
		//	System.out.println( "indexPointer: " + (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L)  + index_offset) );
	 
			unsafe.putInt( (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))  + key_offset,   key);
		//	System.out.println( "keyPointer:   " + (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L)  + key_offset) );
		
			unsafe.putInt( (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))  + value_offset, value);
		//	System.out.println( "valuePointer: " + (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L)  + value_offset) );
			
			unsafe.putInt( (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L))  + parent_offset, parent);
			//	System.out.println( "valuePointer: " + (ADDRESS_POINTER  + ( seed.get() * OBJECT_SIZE * 1L)  + value_offset) );
				
			
		    return seed.get();
	}
	
}
