package memory.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import sun.misc.Unsafe;

public class UnsafeCache<K extends Externalizable, V extends Externalizable> implements Cache<K, V> {
    private final int recordSize;
    private final Unsafe backingMap;
    private final Map<K, Integer> keyToOffset;
    private long address;
    private int capacity;
    private int currentOffset;
    
    

    public UnsafeCache(int recordSize, int maxRecords) {
        this.recordSize = recordSize;
        this.backingMap = getUnsafeBackingMap();
        this.capacity = recordSize * maxRecords;
        this.address = backingMap.allocateMemory(capacity);
        this.keyToOffset = new HashMap<K, Integer>();
    }

    public void put(K key, V value) {
        if(currentOffset + recordSize < capacity) {
            store(currentOffset, value);
            keyToOffset.put(key, currentOffset);
            currentOffset += recordSize;
        }
    }

    public V get(K key) {
        int offset = keyToOffset.get(key);
        if(offset >= 0)
            return retrieve(offset);

        return null;
       // throw new KeyNotFoundException(); 
    }

    public V retrieve(int offset) {
        byte[] record = new byte[recordSize];

        //Inefficient
        for(int i=0; i<record.length; i++) {
            record[i] = backingMap.getByte(address + offset + i);
        }

        //implementation left as an exercise
        return internalize(record);
        
    }

    public void store(int offset, V value) {
        byte[] record = externalize(value);

        //Inefficient
        for(int i=0; i<record.length; i++) {
            backingMap.putByte(address + offset + i, record[i]);
        }
    }

    private Unsafe getUnsafeBackingMap() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) { }
        return null;
    }
    
    public V internalize(byte[] record) {
    	 V obj = null;
         ByteArrayInputStream bis = null;
         ObjectInputStream ois = null;
         try {
             bis = new ByteArrayInputStream(record);
             ois = new ObjectInputStream(bis);
             obj = (V) ois.readObject();
         } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
             if (bis != null) {
                 try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
             }
             if (ois != null) {
                 try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
             }
         }
         return  obj;
    }
    
    public byte[] externalize(V value) {
    	 byte[] bytes = null;
         ByteArrayOutputStream bos = null;
         ObjectOutputStream oos = null;
         try {
             bos = new ByteArrayOutputStream();
             oos = new ObjectOutputStream(bos);
             oos.writeObject(value);
             oos.flush();
             bytes = bos.toByteArray();
         } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
             if (oos != null) {
                 try {
					oos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
             }
             if (bos != null) {
                 try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
             }
         }
         return bytes;
    }

}