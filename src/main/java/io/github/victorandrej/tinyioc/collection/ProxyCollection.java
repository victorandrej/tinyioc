package io.github.victorandrej.tinyioc.collection;

import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Array;
import java.util.*;

public class ProxyCollection<T> implements Collection<T> {
    List<Collection<T>> collections;

    public ProxyCollection(Collection<T>... collections) {
        this.collections = Arrays.asList(collections);
    }

    @Override
    public int size() {
        return collections.stream().map(c -> c.size()).reduce(0, (e1, e2) -> e1 + e2);
    }

    @Override
    public boolean isEmpty() {
        return collections.stream().map(c -> c.size()).reduce(0, (e1, e2) -> e1 + e2) == 0;
    }

    @Override
    public boolean contains(Object o) {
        return collections.stream().anyMatch(c -> c.contains(o));
    }

    @Override
    public Iterator<T> iterator() {
        return new ProxyIterator(collections);
    }

    @Override
    public Object[] toArray() {
        var arr = new Object[this.size()];
        var i = 0;
        for (var el : this)
            arr[i++] = el;
        return arr;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        var arr = Array.newInstance(a.getClass().arrayType(), this.size());
        var i = 0;
        for (var el : this)
            Array.set(arr, i++, el);

        return (T1[]) arr;
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    private class ProxyIterator implements Iterator<T> {
        private Iterator<Collection<T>> iterCollection;
        private Iterator<T> currentIterator;

        public ProxyIterator(Collection<Collection<T>> collections) {
            iterCollection = iterCollection;
        }

        @Override
        public boolean hasNext() {
            return (Objects.nonNull(currentIterator) && currentIterator.hasNext())
                    || iterCollection.hasNext();

        }

        @Override
        public T next() {
            if (Objects.isNull(currentIterator) || !currentIterator.hasNext())
                currentIterator = iterCollection.next().iterator();

            return currentIterator.next();
        }
    }
}
