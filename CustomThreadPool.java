package com.sumukh.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A simple custom thread pool implementation to manage concurrent task execution.
 * 
 * <p>This class demonstrates how thread pools work internally—spawning a fixed number of
 * worker threads that continuously poll and execute submitted tasks.</p>
 *
 * Example:
 * <pre>
 *     CustomThreadPool pool = new CustomThreadPool(3);
 *     pool.execute(() -> System.out.println("Task running"));
 *     pool.shutdown();
 * </pre>
 */
public class CustomThreadPool {

    private final int nThreads;
    private final PoolWorker[] workers;
    private final BlockingQueue<Runnable> taskQueue;
    private volatile boolean isShutdown = false;

    /**
     * Initializes the thread pool with the given number of threads.
     *
     * @param nThreads number of worker threads in the pool
     */
    public CustomThreadPool(int nThreads) {
        this.nThreads = nThreads;
        this.taskQueue = new LinkedBlockingQueue<>();
        this.workers = new PoolWorker[nThreads];

        for (int i = 0; i < nThreads; i++) {
            workers[i] = new PoolWorker("Worker-" + i);
            workers[i].start();
        }
    }

    /**
     * Submits a new task to the thread pool.
     *
     * @param task the task to execute
     * @throws IllegalStateException if pool is shut down
     */
    public void execute(Runnable task) {
        if (isShutdown) {
            throw new IllegalStateException("ThreadPool has been shut down!");
        }
        taskQueue.offer(task);
    }

    /**
     * Shuts down the thread pool gracefully after executing pending tasks.
     */
    public void shutdown() {
        isShutdown = true;
        for (PoolWorker worker : workers) {
            worker.interrupt();
        }
    }

    /**
     * Worker class that continuously takes tasks from the queue and executes them.
     */
    private class PoolWorker extends Thread {
        public PoolWorker(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (!isShutdown || !taskQueue.isEmpty()) {
                try {
                    Runnable task = taskQueue.take();
                    System.out.println(getName() + " executing task...");
                    task.run();
                } catch (InterruptedException ignored) {
                    // Allow thread to exit if shutdown
                }
            }
            System.out.println(getName() + " stopped.");
        }
    }

    // Quick test
    public static void main(String[] args) {
        CustomThreadPool pool = new CustomThreadPool(3);

        for (int i = 1; i <= 10; i++) {
            int taskId = i;
            pool.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " is running task " + taskId);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            });
        }

        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {}

        pool.shutdown();
    }
}
