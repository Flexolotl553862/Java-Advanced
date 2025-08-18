package info.kgeorgiy.ja.morozov.lambda.spliterator;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractTreeSpliterator<T, N> implements Spliterator<T> {

    protected List<N> remainingSubtrees;

    protected long est;

    protected int characteristics;

    private Iterator<T> curIterator;

    public AbstractTreeSpliterator(List<N> remainingSubtrees, int characteristics) {
        this.remainingSubtrees = remainingSubtrees;
        this.est = findSize();
        this.characteristics = Spliterator.ORDERED | characteristics;
        updateCharacteristics();
    }

    protected abstract List<T> getLeaf(N node);

    protected abstract List<N> getChildren(N node);

    protected abstract Spliterator<T> createNew(List<N> remainingSubtrees);

    private void updateIterator() {
        while (!remainingSubtrees.isEmpty()) {
            List<T> leaf = getLeaf(remainingSubtrees.getLast());

            if (!leaf.isEmpty()) {
                curIterator = leaf.iterator();
                remainingSubtrees.removeLast();
                return;
            }

            List<N> children = getChildren(remainingSubtrees.getLast()).reversed();

            remainingSubtrees.removeLast();
            remainingSubtrees.addAll(children);
        }
    }

    protected long getSize(N node) {
        List<N> children = getChildren(node);
        if (children == null) {
            return 1;
        }
        return Long.MAX_VALUE;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (curIterator == null || !curIterator.hasNext()) {
            updateIterator();
        }

        if (curIterator == null || !curIterator.hasNext()) {
            return false;
        }

        action.accept(curIterator.next());
        if (estimateSize() < Long.MAX_VALUE) {
            est--;
        }

        return true;
    }

    @Override
    public long estimateSize() {
        return est;
    }

    @Override
    public int characteristics() {
        return characteristics;
    }

    protected void updateCharacteristics() {
    }

    @Override
    public Spliterator<T> trySplit() {
        if (remainingSubtrees.size() < 2) {
            return createNew(new ArrayList<>());
        }

        int cutPoint = remainingSubtrees.size() / 2;
        Spliterator<T> left = createNew(new ArrayList<>(remainingSubtrees.subList(cutPoint, remainingSubtrees.size())));
        remainingSubtrees = remainingSubtrees.subList(0, cutPoint);
        est = findSize();
        if (est < Long.MAX_VALUE) {
            characteristics |= Spliterator.SIZED | Spliterator.SUBSIZED;
        }
        return left;
    }

    private long findSize() {
        long size = 0;
        for (N subtree : remainingSubtrees) {
            if (getSize(subtree) < Long.MAX_VALUE) {
                size += getSize(subtree);
            } else {
                return Long.MAX_VALUE;
            }
        }
        return size;
    }
}
