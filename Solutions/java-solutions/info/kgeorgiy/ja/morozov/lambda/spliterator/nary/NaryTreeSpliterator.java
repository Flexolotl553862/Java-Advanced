package info.kgeorgiy.ja.morozov.lambda.spliterator.nary;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.*;

public class NaryTreeSpliterator<T> extends BasicTreeSpliterator<T, Trees.Nary<T>> {

    public NaryTreeSpliterator(Trees.Nary<T> tree) {
        super(new ArrayList<>(List.of(tree)), 0);
    }

    public NaryTreeSpliterator(List<Trees.Nary<T>> remainingSubtrees) {
        super(remainingSubtrees, 0);
    }

    @Override
    protected List<T> getLeaf(Trees.Nary<T> node) {
        if (node instanceof Trees.Leaf<T>) {
            return List.of(((Trees.Leaf<T>) node).value());
        }
        return new ArrayList<>();
    }

    @Override
    protected List<Trees.Nary<T>> getChildren(Trees.Nary<T> element) {
        if (element instanceof Trees.Nary.Node<T> node) {
            return node.children();
        }
        return null;
    }

    @Override
    protected Spliterator<T> createNew(List<Trees.Nary<T>> remainingSubtrees) {
        return new NaryTreeSpliterator<>(remainingSubtrees);
    }
}
