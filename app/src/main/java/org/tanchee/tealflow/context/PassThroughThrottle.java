package org.tanchee.tealflow.context;

import org.tanchee.txt.core.throttle.ThrottleHandle;

/**
 * A {@link ThrottleHandle} that always grants a permit.
 *
 * <p>Used by {@link SimpleThrottleRegistry} as the default throttle for all
 * components in local/test contexts where rate limiting is not required.
 *
 * <p>Replace with a {@link org.tanchee.txt.core.throttle.TokenBucketThrottle}
 * per component when running against live external sources (Reddit API, RSS feeds, etc.).
 */
public final class PassThroughThrottle implements ThrottleHandle {

    public static final PassThroughThrottle INSTANCE = new PassThroughThrottle();

    private PassThroughThrottle() {}

    @Override
    public boolean tryAcquire() {
        return true;
    }
}
