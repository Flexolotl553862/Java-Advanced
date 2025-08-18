package info.kgeorgiy.ja.morozov.iterative;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * This class realized parallel queue of {@link Runnable}.
 */
public class TaskQueue {
    private final Deque<Runnable> tasks = new ArrayDeque<>();

    /**
     * Add a new task in queue.
     * @param task task that will add in queue
     */
    public synchronized void putTask(Runnable task) {
        tasks.add(task);
        notify();
    }

    /**
     * Returns the first task from this queue.
     * @return first task in this queue.
     * @throws InterruptedException If the thread from which the method was called was interrupted.
     */
    public synchronized Runnable removeTask() throws InterruptedException {
        while(tasks.isEmpty()) {
            wait();
        }
        return tasks.removeFirst();
    }
}
