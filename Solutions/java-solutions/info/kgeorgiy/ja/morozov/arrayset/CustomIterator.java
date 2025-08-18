package info.kgeorgiy.ja.morozov.arrayset;

import java.util.Iterator;

public class CustomIterator<T> implements Iterator<T> {
    private final Iterator<T> iterator;

    public CustomIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
