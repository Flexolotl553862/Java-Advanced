package info.kgeorgiy.ja.morozov.lambda.spliterator.nary;

import info.kgeorgiy.ja.morozov.lambda.spliterator.AbstractTreeSpliterator;

import java.util.List;
import java.util.Spliterator;

public abstract class BasicTreeSpliterator<T, N> extends AbstractTreeSpliterator<T, N> {

    public BasicTreeSpliterator(List<N> remainingSubtrees, int characteristics) {
        super(remainingSubtrees, characteristics | Spliterator.IMMUTABLE | Spliterator.SUBSIZED);
    }

    @Override
    protected void updateCharacteristics() {
        if (remainingSubtrees.size() == 1 && getChildren(remainingSubtrees.getFirst()) == null) {
            characteristics |= Spliterator.SIZED;
        }
    }
}
