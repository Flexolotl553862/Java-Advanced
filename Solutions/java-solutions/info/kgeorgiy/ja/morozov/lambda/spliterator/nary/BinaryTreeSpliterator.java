package info.kgeorgiy.ja.morozov.lambda.spliterator.nary;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

public class BinaryTreeSpliterator<T> extends BasicTreeSpliterator<T, Trees.Binary<T>> {

    public BinaryTreeSpliterator(Trees.Binary<T> tree) {
        super(new ArrayList<>(List.of(tree)), 0);
    }

    public BinaryTreeSpliterator(List<Trees.Binary<T>> remainingSubtrees) {
        super(remainingSubtrees, 0);
    }

    @Override
    protected List<T> getLeaf(Trees.Binary<T> node) {
        if (node instanceof Trees.Leaf<T> leaf) {
            return List.of(leaf.value());
        }
        return new ArrayList<>();
    }

    @Override
    protected List<Trees.Binary<T>> getChildren(Trees.Binary<T> node) {
        if (node instanceof Trees.Binary.Branch<T> branch) {
            return new ArrayList<>(List.of(branch.left(), branch.right()));
        }
        return null;
    }

    @Override
    protected Spliterator<T> createNew(List<Trees.Binary<T>> remainingSubtrees) {
        return new BinaryTreeSpliterator<>(remainingSubtrees);
    }
}
