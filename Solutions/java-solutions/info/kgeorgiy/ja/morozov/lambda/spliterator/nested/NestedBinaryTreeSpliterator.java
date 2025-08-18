package info.kgeorgiy.ja.morozov.lambda.spliterator.nested;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.*;

public class NestedBinaryTreeSpliterator<T> extends NestedTreeSpliterator<T, Trees.Binary<List<T>>> {

    public NestedBinaryTreeSpliterator(Trees.Binary<List<T>> tree) {
        super(new ArrayList<>(List.of(tree)));
    }

    public NestedBinaryTreeSpliterator(List<Trees.Binary<List<T>>> remainingSubtrees) {
        super(remainingSubtrees);
    }

    @Override
    protected List<T> getLeaf(Trees.Binary<List<T>> tree) {
        if (tree instanceof Trees.Leaf<List<T>> leaf && !leaf.value().isEmpty()) {
            return leaf.value();
        }
        return new ArrayList<>();
    }

    @Override
    protected List<Trees.Binary<List<T>>> getChildren(Trees.Binary<List<T>> node) {
        if (node instanceof Trees.Binary.Branch<List<T>> branch) {
            return new ArrayList<>(List.of(branch.left(), branch.right()));
        }
        return null;
    }

    @Override
    protected Spliterator<T> createNew(List<Trees.Binary<List<T>>> remainingSubtrees) {
        return new NestedBinaryTreeSpliterator<>(remainingSubtrees);
    }
}
