package info.kgeorgiy.ja.morozov.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final Comparator<? super T> comparator;

    private final List<T> elements;

    public ArraySet(List<T> list, Comparator<? super T> comparator, boolean isSorted) {
        this.comparator = comparator;

        if (isSorted) {
            this.elements = list;
        } else {
            TreeSet<T> treeSet = new TreeSet<>(comparator);
            treeSet.addAll(list);
            this.elements = new ArrayList<>(treeSet);
        }
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator, boolean isSorted) {
        this(new ArrayList<>(collection), comparator, isSorted);
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        this(collection, comparator, false);
    }

    public ArraySet(Collection<? extends T> collection) {
        this(collection, null);
    }

    public ArraySet() {
        this(new ArrayList<>(), null, true);
    }

    private int compare(T e1, T e2) {
        if (comparator == null) {
            return Collections.reverseOrder().reversed().compare(e1, e2);
        }

        return comparator.compare(e1, e2);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public T first() {
        return elements.getFirst();
    }

    @Override
    public T last() {
        return elements.getLast();
    }

    @Override
    public Iterator<T> iterator() {
        return new CustomIterator<>(elements.iterator());
    }

    private T getOrNull(int index) {
        if (index < 0 || index >= elements.size()) {
            return null;
        }
        return elements.get(index);
    }

    private int getIndex(T element) {
        return Collections.binarySearch(elements, element, comparator);
    }

    private int findInsertionIndex(T element, boolean inclusive, int offset) throws NullPointerException {
        int index = getIndex(element);

        if (index < 0) {
            if (offset == -1) {
                return -index - 2;
            }
            return -index - 1;
        } else {
            return inclusive ? index : index + offset;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        T element = (T) o;
        return getIndex(element) >= 0;
    }

    @Override
    public T ceiling(T t) {
        return getOrNull(findInsertionIndex(t, true, 1));
    }

    @Override
    public T floor(T t) {
        return getOrNull(findInsertionIndex(t, true, -1));
    }

    @Override
    public T higher(T t) {
        return getOrNull(findInsertionIndex(t, false, 1));
    }

    @Override
    public T lower(T t) {
        return getOrNull(findInsertionIndex(t, false, -1));
    }

    private NavigableSet<T> subArraySet(int l, int r) {
        if (l < 0 || l >= elements.size() || r < 0 || r >= elements.size() || l > r) {
            ArrayList<T> list = new ArrayList<>();
            return new ArraySet<>(list, comparator, true);
        }

        return new ArraySet<>(elements.subList(l, r + 1), comparator, true);
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {

        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }

        return subArraySet(
                findInsertionIndex(fromElement, fromInclusive, 1),
                findInsertionIndex(toElement, toInclusive, -1));
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return subArraySet(0, findInsertionIndex(toElement, inclusive, -1));
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return subArraySet(findInsertionIndex(fromElement, inclusive, 1), size() - 1);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(elements.reversed(), Collections.reverseOrder(comparator), true);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public boolean addAll(Collection<? extends T> c) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public boolean retainAll(Collection<?> c) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public boolean removeAll(Collection<?> c) {
//        throw new UnsupportedOperationException();
//    }

//    @Override
//    public void clear() {
//        throw new UnsupportedOperationException();
//    }
}
