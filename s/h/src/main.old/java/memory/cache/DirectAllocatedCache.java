package memory.cache;

import java.io.Externalizable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class DirectAllocatedCache<K extends Externalizable, V extends Externalizable> implements Cache<K,V> {
    private final ByteBuffer backingMap;
    private final Map<K, Integer> keyToOffset;
    private final int recordSize;

    public DirectAllocatedCache(int recordSize, int maxRecords) {
        this.recordSize = recordSize;
        this.backingMap = ByteBuffer.allocateDirect(recordSize * maxRecords);
        this.keyToOffset = new HashMap<K, Integer>();
    }

    public void put(K key, V value) {
        if(backingMap.position() + recordSize < backingMap.capacity()) {
            keyToOffset.put(key, backingMap.position());
            store(value);
        }   
    }

    public V get(K key) {
        int offset = keyToOffset.get(key);
        if(offset >= 0)
            return retrieve(offset);

        return null;
      //  throw new KeyNotFoundException();
    }
 
    public V retrieve(int offset) {
        byte[] record = new byte[recordSize];
        int oldPosition = backingMap.position();
        backingMap.position(offset);
        backingMap.get(record);
        backingMap.position(oldPosition);

        //implementation left as an exercise
        return internalize(record);
    }

    public void store(V value) {
        byte[] record = externalize(value);
        backingMap.put(record);
    }
    
    public V internalize(byte[] record) {
    	return null;
    }
    
    public byte[] externalize(V value) {
    	return null;
    }
    
}