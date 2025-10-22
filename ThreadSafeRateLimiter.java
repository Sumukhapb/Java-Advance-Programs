import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSafeRateLimiter {
    private final int maxRequests;
    private final long timeWindowMillis;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private long windowStartTime;

    public ThreadSafeRateLimiter(int maxRequests, long timeWindowMillis) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindowMillis;
        this.windowStartTime = System.currentTimeMillis();
    }

    // Returns true if the request is allowed, false if rate limit exceeded
    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();

        if (now - windowStartTime > timeWindowMillis) {
            // reset the window
            requestCount.set(0);
            windowStartTime = now;
        }

        if (requestCount.get() < maxRequests) {
            requestCount.incrementAndGet();
            return true;
        } else {
            return false;
        }
    }

    // Test simulation
    public static void main(String[] args) throws InterruptedException {
        ThreadSafeRateLimiter limiter = new ThreadSafeRateLimiter(5, 1000); // 5 req/sec

        for (int i = 1; i <= 10; i++) {
            boolean allowed = limiter.allowRequest();
            System.out.println("Request " + i + " -> " + (allowed ? "ALLOWED" : "BLOCKED"));
            Thread.sleep(150); // simulate 150ms between requests
        }
    }
}
