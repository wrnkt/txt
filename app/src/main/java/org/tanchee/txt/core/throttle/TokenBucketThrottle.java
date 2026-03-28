package org.tanchee.txt.core.throttle;

import java.util.concurrent.atomic.AtomicInteger;

public final class TokenBucketThrottle implements ThrottleHandle {
    private final AtomicInteger tokens = new AtomicInteger();

    public boolean tryAcquire() {
        while (true) {
            int current = tokens.get();
            if (current <= 0) {
                return false;
            }
            if (tokens.compareAndSet(current, current - 1)) {
                return true;
            }
        }
    }
}
