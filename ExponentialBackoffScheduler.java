package com.sumukh.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Exponential Backoff Task Scheduler.
 *
 * <p>This scheduler retries failed tasks with exponentially increasing delays.
 * Commonly used in network systems, APIs, and distributed job schedulers.</p>
 */
public class ExponentialBackoffScheduler {

    private final ExecutorService executor;
    private final int maxRetries;
    private final long baseDelayMillis;

    /**
     * @param threadCount Number of worker threads
     * @param maxRetries Maximum number of retries per task
     * @param baseDelayMillis Initial delay before first retry (in ms)
     */
    public ExponentialBackoffScheduler(int threadCount, int maxRetries, long baseDelayMillis) {
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.maxRetries = maxRetries;
        this.baseDelayMillis = baseDelayMillis;
    }

    /**
     * Submits a task with exponential backoff retry behavior.
     *
     * @param task Task that may fail and require retries
     */
    public void submit(Runnable task) {
        executor.submit(() -> runWithRetry(task, 0));
    }

    private void runWithRetry(Runnable task, int attempt) {
        try
