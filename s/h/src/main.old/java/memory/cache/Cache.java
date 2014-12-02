package memory.cache;

import java.io.Externalizable;

public interface Cache<K extends Externalizable, V extends Externalizable> {
    public void put(K key, V value);
    public V get(K key);
}