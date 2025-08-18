package info.kgeorgiy.ja.morozov.lambda;

import info.kgeorgiy.ja.morozov.lambda.spliterator.nary.BinaryTreeSpliterator;
import info.kgeorgiy.ja.morozov.lambda.spliterator.nested.NestedBinaryTreeSpliterator;
import info.kgeorgiy.ja.morozov.lambda.spliterator.nested.NestedSizedBinaryTreeSpliterator;
import info.kgeorgiy.ja.morozov.lambda.spliterator.nary.SizedBinaryTreeSpliterator;
import info.kgeorgiy.ja.morozov.lambda.spliterator.nary.NaryTreeSpliterator;
import info.kgeorgiy.ja.morozov.lambda.spliterator.nested.NestedNaryTreeSpliterator;
import info.kgeorgiy.java.advanced.lambda.EasyLambda;
import info.kgeorgiy.java.advanced.lambda.HardLambda;
import info.kgeorgiy.java.advanced.lambda.Trees;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class Lambda implements EasyLambda, HardLambda {
    @Override
    public <T> Spliterator<T> binaryTreeSpliterator(Trees.Binary<T> tree) {
        return new BinaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Spliterator<T> sizedBinaryTreeSpliterator(Trees.SizedBinary<T> tree) {
        return new SizedBinaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Spliterator<T> naryTreeSpliterator(Trees.Nary<T> tree) {
        return new NaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Collector<T, ?, Optional<T>> first() {
        return kth(0);
    }

    @Override
    public <T> Collector<T, ?, Optional<T>> last() {
        return Collectors.reducing((e1, e2) -> e2);
    }

    @Override
    public <T> Collector<T, ?, Optional<T>> middle() {
        return Collectors.collectingAndThen(Collectors.reducing(
                new AbstractMap.SimpleImmutableEntry<>(new ArrayDeque<>(), false),
                (T e) -> new AbstractMap.SimpleImmutableEntry<>(new ArrayDeque<>(List.of(e)), false),
                (d1, d2) -> {
                    Deque<T> res = new ArrayDeque<>(d1.getKey());
                    boolean flag = d1.getValue();
                    res.addFirst(d2.getKey().getFirst());
                    if (flag) {
                        res.removeLast();
                    }
                    return new AbstractMap.SimpleImmutableEntry<>(res, !flag);
                }
        ), (AbstractMap.SimpleImmutableEntry<Deque<T>, Boolean> p) -> {
            if (p.getKey().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(p.getKey().getLast());
        });
    }

    @Override
    public Collector<CharSequence, ?, String> commonPrefix() {
        return Collectors.collectingAndThen(Collectors.reducing((CharSequence s1, CharSequence s2) -> {
            int i = 0;

            while (i < min(s1.length(), s2.length()) && s1.charAt(i) == s2.charAt(i)) {
                i++;
            }

            return s1.subSequence(0, i);
        }), s -> s.map(CharSequence::toString).orElse(""));
    }

    @Override
    public Collector<CharSequence, ?, String> commonSuffix() {
        return Collectors.collectingAndThen(Collectors.reducing((CharSequence s1, CharSequence s2) -> {
            int i = 0;

            while (i < min(s1.length(), s2.length()) &&
                    s1.charAt(s1.length() - i - 1) == s2.charAt(s2.length() - i - 1)) {
                i++;
            }

            return s1.subSequence(s1.length() - i, s1.length());
        }), s -> s.map(CharSequence::toString).orElse(""));
    } // :NOTE: commonSuffix и commonPrefix все еще можно обобщить

    @Override
    public <T> Spliterator<T> nestedBinaryTreeSpliterator(Trees.Binary<List<T>> tree) {
        return new NestedBinaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Spliterator<T> nestedSizedBinaryTreeSpliterator(Trees.SizedBinary<List<T>> tree) {
        return new NestedSizedBinaryTreeSpliterator<>(tree);
    }

    @Override
    public <T> Spliterator<T> nestedNaryTreeSpliterator(Trees.Nary<List<T>> tree) {
        return new NestedNaryTreeSpliterator<>(tree);
    }

    private <T> Collector<T, ?, List<T>> slidingWindow(
            BiConsumer<ArrayDeque<T>, T> accumulator,
            BinaryOperator<ArrayDeque<T>> combiner
    ) {
        return Collector.of(
                ArrayDeque::new,
                accumulator,
                combiner,
                acc -> acc.stream().toList()
        );
    }

    @Override
    public <T> Collector<T, ?, List<T>> head(int k) {
        return slidingWindow(
                (acc, e) -> {
                    if (acc.size() < k) {
                        acc.add(e);
                    }
                },
                (a1, a2) -> {
                    while (a1.size() < k && !a2.isEmpty()) {
                        a1.addLast(a2.removeFirst());
                    }
                    return a1;
                });
    }

    @Override
    public <T> Collector<T, ?, List<T>> tail(int k) {
        return slidingWindow(
                (acc, e) -> {
                    if (k > 0 && acc.size() >= k) {
                        acc.removeFirst();
                    }
                    if (acc.size() < k) {
                        acc.addLast(e);
                    }
                },
                (a1, a2) -> {
                    while (a2.size() < k && !a1.isEmpty()) {
                        a2.addFirst(a1.removeLast());
                    }
                    return a2;
                });
    }

    @Override
    public <T> Collector<T, ?, Optional<T>> kth(int k) {
        return Collectors.collectingAndThen(
                Collectors.reducing(
                        new AbstractMap.SimpleImmutableEntry<>(Optional.empty(), k + 1),
                        (T e) -> new AbstractMap.SimpleImmutableEntry<>(Optional.of(e), 0),
                        (p1, p2) -> {
                            if (p1.getValue() > 0) {
                                return new AbstractMap.SimpleImmutableEntry<>(
                                        p2.getKey(),
                                        p1.getValue() - 1
                                );
                            }
                            return p1;
                        }),
                (Map.Entry<Optional<T>, Integer> p) -> {
                    if (p.getValue() == 0 && k >= 0) {
                        return p.getKey();
                    }
                    return Optional.empty();
                }
        );
    }
}
