package org.tanchee.txt.core.throttle;

public interface ThrottleHandle {
    boolean tryAcquire();
}
