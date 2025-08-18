package info.kgeorgiy.ja.morozov.lambda.spliterator.nested;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.*;

public class NestedSizedBinaryTreeSpliterator<T> extends NestedTreeSpliterator<T, Trees.SizedBinary<List<T>>> {

    public NestedSizedBinaryTreeSpliterator(Trees.SizedBinary<List<T>> tree) {
        super(new ArrayList<>(List.of(tree)));
    }

    public NestedSizedBinaryTreeSpliterator(List<Trees.SizedBinary<List<T>>> remainingSubtrees) {
        super(remainingSubtrees);
    }

    @Override
    protected List<T> getLeaf(Trees.SizedBinary<List<T>> tree) {
        if (tree instanceof Trees.Leaf<List<T>> leaf) {
            return leaf.value();
        }
        return new ArrayList<>();
    }

    @Override
    protected List<Trees.SizedBinary<List<T>>> getChildren(Trees.SizedBinary<List<T>> node) {
        if (node instanceof Trees.SizedBinary.Branch<List<T>> branch) {
            return new ArrayList<>(List.of(branch.left(), branch.right()));
        }
        return null;
    }

    @Override
    protected Spliterator<T> createNew(List<Trees.SizedBinary<List<T>>> remainingSubtrees) {
        return new NestedSizedBinaryTreeSpliterator<>(remainingSubtrees);
    }

    @Override
    protected long getSize(Trees.SizedBinary<List<T>> node) {
        return node.size();
    }
}
