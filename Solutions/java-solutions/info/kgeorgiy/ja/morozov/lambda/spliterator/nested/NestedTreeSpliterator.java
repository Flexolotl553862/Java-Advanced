package info.kgeorgiy.ja.morozov.lambda.spliterator.nested;

import info.kgeorgiy.ja.morozov.lambda.spliterator.AbstractTreeSpliterator;

import java.util.List;

public abstract class NestedTreeSpliterator<T, N> extends AbstractTreeSpliterator<T, N> {

    public NestedTreeSpliterator(List<N> remainingSubtrees) {
        super(remainingSubtrees, 0);
    }
}
