package info.kgeorgiy.ja.morozov.iterative;

import info.kgeorgiy.java.advanced.iterative.ListIP;
import info.kgeorgiy.java.advanced.iterative.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class can be used to perform parallel operations with {@link List}.
 *
 * @see ScalarIP
 * @see ListIP
 */
public class IterativeParallelism implements ScalarIP, ListIP {
    private final ParallelMapper parallelMapper;

    /**
     * Default constructor.
     * An instance won't use {@link ParallelMapper} for parallel operations.
     */
    public IterativeParallelism() {
        parallelMapper = null;
    }

    /**
     * This constructor creates an instance that uses
     * {@link ParallelMapper} for parallel operations.
     *
     * @param parallelMapper an implementation of {@link ParallelMapper} used for parallel operations
     */
    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private <T, Q> Q parallelListReduce(int threadsCnt,
                                        List<T> values,
                                        Supplier<Q> identity,
                                        Function<Integer, Q> function,
                                        BinaryOperator<Q> merge
    ) throws InterruptedException {
        final int blockSize = values.size() / threadsCnt;
        List<Q> results = new ArrayList<>(Collections.nCopies(threadsCnt, null));
        int idx = 0;
        int left = 0;
        int sz = values.size() % threadsCnt;
        List<Thread> threads = new ArrayList<>();
        List<IntStream> streams = new ArrayList<>();
        Function<IntStream, Q> u = (IntStream stream) ->
                stream.mapToObj(function::apply).reduce(identity.get(), merge);
        while (idx < threadsCnt) {
            final int curLeft = left;
            final int curIdx = idx;
            final int curSz = sz <= 0 ? blockSize : blockSize + 1;
            IntStream stream = IntStream.range(curLeft, Math.min(curLeft + curSz, values.size()));
            if (parallelMapper == null) {
                Thread thread = new Thread(() -> results.set(curIdx, u.apply(stream)));
                threads.add(thread);
                thread.start();
            } else {
                streams.add(stream);
            }
            idx++;
            left += curSz;
            sz--;
        }
        for (Thread thread : threads) {
            thread.join();
        }
        if (parallelMapper != null) {
            idx = 0;
            for (Q res : parallelMapper.map(u, streams)) {
                results.set(idx, res);
                idx++;
            }
        }
        return results.stream().reduce(identity.get(), merge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> int argMax(int threads, List<T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelListReduce(threads,
                values,
                () -> -1,
                idx -> idx,
                (acc, idx) -> idx != -1
                        && (acc == -1 || comparator.compare(values.get(acc), values.get(idx)) < 0) ? idx : acc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> int argMin(int threads, List<T> values, Comparator<? super T> comparator) throws InterruptedException {
        return argMax(threads, values, comparator.reversed());
    }

    private <T> int commonIndexOf(int threads, List<T> values, Predicate<? super T> predicate, boolean rightPriority)
            throws InterruptedException {
        return parallelListReduce(threads,
                values,
                () -> -1,
                idx -> idx,
                (acc, idx) -> (acc == -1 || rightPriority) && idx != -1 && predicate.test(values.get(idx)) ? idx : acc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> int indexOf(int threads, List<T> values, Predicate<? super T> predicate) throws InterruptedException {
        return commonIndexOf(threads, values, predicate, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> int lastIndexOf(int threads, List<T> values, Predicate<? super T> predicate) throws InterruptedException {
        return commonIndexOf(threads, values, predicate, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> long sumIndices(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {

        return parallelListReduce(threads,
                values,
                () -> (long) 0,
                idx -> predicate.test(values.get(idx)) ? (long) idx : (long) 0,
                Long::sum);
    }

    private <T, R> Stream<R> streamConcatOperation(int threads, List<? extends T> values, Function<Integer, Stream<R>> f)
            throws InterruptedException {
        return parallelListReduce(threads, values, Stream::empty, f, Stream::concat);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> int[] indices(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return streamConcatOperation(threads,
                values,
                (idx) -> {
                    if (predicate.test(values.get(idx))) {
                        return Stream.of(idx);
                    }
                    return Stream.empty();
                }).mapToInt(Integer::intValue).toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return streamConcatOperation(threads,
                values,
                (idx) -> {
                    if (predicate.test(values.get(idx))) {
                        return Stream.of(values.get(idx));
                    }
                    return Stream.empty();
                }).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f)
            throws InterruptedException {
        Objects.requireNonNull(f, "function cannot be null");
        return streamConcatOperation(
                threads,
                values,
                idx -> Stream.of(f.apply(values.get(idx)))).collect(Collectors.toList());
    }
}
