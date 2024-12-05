package io.github.victorandrej.tinyioc.collection;

import java.util.*;

public class ParentMap<K, V> implements Map<K, V> {
    private final Map<K, V> instance = new LinkedHashMap<>();
    private final Collection<Map<K, V>> parents;

    public ParentMap(Collection<Map<K, V>> parents) {
        this.parents = parents;
    }

    @Override
    public int size() {
        return instance.size();
    }

    @Override
    public boolean isEmpty() {
        return instance.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return instance.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return instance.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return instance.get(key);
    }

    @Override
    public V put(K key, V value) {
        this.parents.forEach(m -> m.put(key, value));
        return this.instance.put(key, value);
    }

    @Override
    public V remove(Object key) {
        this.parents.forEach(m -> m.remove(key));
        return this.instance.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.parents.forEach(map -> map.putAll(m));
        this.instance.putAll(m);
    }

    @Override
    public void clear() {
        this.parents.forEach(m -> this.instance.keySet().forEach(m::remove));
    }

    @Override
    public Set<K> keySet() {
        return instance.keySet();
    }

    @Override
    public Collection<V> values() {
        return instance.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return instance.entrySet();
    }
}
