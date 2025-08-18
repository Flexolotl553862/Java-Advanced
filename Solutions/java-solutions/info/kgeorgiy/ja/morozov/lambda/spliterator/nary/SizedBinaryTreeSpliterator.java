package info.kgeorgiy.ja.morozov.lambda.spliterator.nary;

import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

public class SizedBinaryTreeSpliterator<T> extends BasicTreeSpliterator<T, Trees.SizedBinary<T>> {

    public SizedBinaryTreeSpliterator(Trees.SizedBinary<T> tree) {
        super(new ArrayList<>(List.of(tree)), Spliterator.SIZED);
    }

    public SizedBinaryTreeSpliterator(List<Trees.SizedBinary<T>> remainingSubtrees) {
        super(remainingSubtrees, Spliterator.SIZED);
    }

    @Override
    protected List<T> getLeaf(Trees.SizedBinary<T> node) {
        if (node instanceof Trees.Leaf<T> leaf) {
            return List.of(leaf.value());
        }
        return new ArrayList<>();
    }

    @Override
    protected List<Trees.SizedBinary<T>> getChildren(Trees.SizedBinary<T> node) {
        if (node instanceof Trees.SizedBinary.Branch<T> branch) {
            return new ArrayList<>(List.of(branch.left(), branch.right()));
        }
        return null;
    }

    @Override
    protected Spliterator<T> createNew(List<Trees.SizedBinary<T>> remainingSubtrees) {
        return new SizedBinaryTreeSpliterator<>(remainingSubtrees);
    }

    @Override
    protected long getSize(Trees.SizedBinary<T> node) {
        return node.size();
    }
}
