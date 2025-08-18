package info.kgeorgiy.ja.morozov.iterative;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * This class implements {@link ParallelMapper}.
 */
public class ParallelMapperImpl implements ParallelMapper, AutoCloseable {
    private final List<Thread> workers;
    private final TaskQueue taskQueue;
    private volatile boolean isClosed = false;
    private final Set<Object> locks = new HashSet<>();

    /**
     * This constructor creates an instance that
     * will use a given number of working threads for doing parallel operations.
     *
     * @param threads number of working threads.
     */
    public ParallelMapperImpl(int threads) {
        workers = new ArrayList<>(threads);
        taskQueue = new TaskQueue();
        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        taskQueue.removeTask().run();
                    } catch (InterruptedException ignored) {
                        return;
                    }
                }
            }));
        }
        workers.forEach(Thread::start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> items) throws InterruptedException {
        if (isClosed) {
            throw new IllegalStateException("Mapper is closed");
        }
        final int[] counter = {0};
        final RuntimeException[] exception = {null};
        Object lock = new Object();
        List<R> answer = new ArrayList<>(Collections.nCopies(items.size(), null));

        synchronized (locks) {
            locks.add(lock);
        }

        for (int i = 0; i < items.size(); i++) {
            final int idx = i;
            taskQueue.putTask(() -> {
                try {
                    answer.set(idx, f.apply(items.get(idx)));
                } catch (RuntimeException e) {
                    synchronized (lock) {
                        exception[0] = e;
                        lock.notify();
                    }
                }
                synchronized (lock) {
                    counter[0]++;
                    lock.notify(); // :NOTE: redundant most of the time
                }
            });
        }

        synchronized (lock) {
            while (counter[0] < items.size() && exception[0] == null && !isClosed) {
                lock.wait();
            }
            synchronized (locks) {
                locks.remove(lock);
            }
            if (isClosed) {
                throw new InterruptedException();
            }
            if (exception[0] != null) {
                throw exception[0];
            }
            return answer;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        isClosed = true;
        for (Thread worker : workers) {
            worker.interrupt();
        }
        synchronized (locks) {
            for (Object lock : locks) {
                synchronized (lock) {
                    lock.notify();
                }
            }
        }
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException ignored) {

            }
        }
    }
}
