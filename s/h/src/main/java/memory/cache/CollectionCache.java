package memory.cache;

import java.io.Externalizable;
import java.util.HashMap;
import java.util.Map;

public class CollectionCache<K extends Externalizable, V extends Externalizable> implements Cache<K, V> {
    private final Map<K, V> backingMap = new HashMap<K, V>();

    public void put(K key, V value) {
        backingMap.put(key, value);
    }

    public V get(K key) {
        return backingMap.get(key);
    }
}