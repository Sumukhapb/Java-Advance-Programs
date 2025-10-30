package com.sumukh.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread-safe Token Bucket Rate Limiter implementation.
 * 
 * <p>This class limits how many actions (like API requests) can occur per second.
 * It refills tokens periodically, representing available request slots.</p>
 *
 * Example:
 * <pre>
 *     TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1);
 *     if (limiter.allowRequest()) {
 *         // process request
 *     }
 * </pre>
 */
public class TokenBucketRateLimiter {

    private final int capacity;               // max tokens
    private final int refillRatePerSecond;    // tokens added each second
    private final AtomicInteger tokens;       // current tokens
    private long lastRefillTimestamp;         // last refill time in ms

    /**
     * Initializes a Token Bucket rate limiter.
     *
     * @param capacity max number of tokens in the bucket
     * @param refillRatePerSecond number of tokens to refill every second
     */
    public TokenBucketRateLimiter(int capacity, int refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.tokens = new AtomicInteger(capacity);
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    /**
     * Tries to acquire permission for a request.
     * 
     * @return true if allowed, false otherwise
     */
    public synchronized boolean allowRequest() {
        refill();

        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * Refills the tokens based on elapsed time.
     */
    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTimestamp;

        if (elapsed > 1000) {
            int tokensToAdd = (int) ((elapsed / 1000) * refillRatePerSecond);
            int newTokenCount = Math.min(capacity, tokens.get() + tokensToAdd);
            tokens.set(newTokenCount);
            lastRefillTimestamp = now;
        }
    }

    // Quick test
    public static void main(String[] args) throws InterruptedException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 2);

        for (int i = 1; i <= 15; i++) {
            boolean allowed = limiter.allowRequest();
            System.out.printf("Request %d -> %s%n", i, allowed ? "✅ Allowed" : "❌ Blocked");
            Thread.sleep(300);
        }
    }
}
