package io.github.victorandrej.tinyioc.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Set que reflete qualquer operacao nos seus parents
 * @param <T>
 */
public class ParentSet<T> implements Set<T> {
    private final Set<T> instance = new HashSet<>();
    private final Collection<Set<T>> parent;

    public ParentSet(Collection<Set<T>> parent) {
        this.parent = parent;
    }


    @Override
    public int size() {
        return this.instance.size();
    }

    @Override
    public boolean isEmpty() {
        return this.instance.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.instance.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return this.instance.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.instance.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return this.instance.toArray(a);
    }

    @Override
    public boolean add(T t) {
        var retorno = true;
        for (var s : this.parent) {
            var v = s.add(t);
            retorno = !retorno ? retorno : v;
        }

        return this.instance.add(t) && retorno;
    }

    @Override
    public boolean remove(Object o) {

        var retorno = true;
        for (var s : this.parent) {
            var v = s.remove(o);
            retorno = !retorno ? retorno : v;
        }

        return this.instance.remove(o) && retorno;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.instance.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        var retorno = true;
        for (var s : this.parent) {
            var v = s.addAll(c);
            retorno = !retorno ? retorno : v;
        }
        return this.instance.addAll(c) && retorno;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        var tempSet = new HashSet<>(this.instance);
        for (var el : c)
            tempSet.remove(c);
        var retorno = true;
        for (var s : this.parent) {
            var v = s.removeAll(tempSet);
            retorno = !retorno ? retorno : v;
        }
        return this.instance.retainAll(c) && retorno;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        var retorno = true;
        for (var s : this.parent) {
            var v = s.removeAll(c);
            retorno = !retorno ? retorno : v;
        }
        return this.instance.removeAll(c) && retorno;
    }

    @Override
    public void clear() {
        for (var s : this.parent) {
            var v = s.removeAll(this.instance);
        }
        this.instance.clear();
    }
}
