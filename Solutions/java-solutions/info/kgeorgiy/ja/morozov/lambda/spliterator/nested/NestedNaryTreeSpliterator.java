package info.kgeorgiy.ja.morozov.lambda.spliterator.nested;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

public class NestedNaryTreeSpliterator<T> extends NestedTreeSpliterator<T, Trees.Nary<List<T>>> {
    
    public NestedNaryTreeSpliterator(Trees.Nary<List<T>> tree) {
        super(new ArrayList<>(List.of(tree)));
    }

    public NestedNaryTreeSpliterator(List<Trees.Nary<List<T>>> remainingSubtrees) {
        super(remainingSubtrees);
    }

    @Override
    protected List<T> getLeaf(Trees.Nary<List<T>> tree) {
        if (tree instanceof Trees.Leaf<List<T>> leaf && !leaf.value().isEmpty()) {
            return leaf.value();
        }
        return new ArrayList<>();
    }

    @Override
    protected List<Trees.Nary<List<T>>> getChildren(Trees.Nary<List<T>> element) {
        if (element instanceof Trees.Nary.Node<List<T>> node) {
            return node.children();
        }
        return null;
    }

    @Override
    protected Spliterator<T> createNew(List<Trees.Nary<List<T>>> remainingSubtrees) {
        return new NestedNaryTreeSpliterator<>(remainingSubtrees);
    }
}
