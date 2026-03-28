package org.tanchee.txt.core.stats;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class ComponentStats {
    private final LongAdder processed = new LongAdder();
    private final LongAdder failed = new LongAdder();
    private final AtomicInteger activeWorkers = new AtomicInteger();
    private final AtomicInteger queued = new AtomicInteger();

    public void processed() {
        processed.increment();
    }

    public void failed() {
        failed.increment();
    }

    public void workerStarted() {
        activeWorkers.incrementAndGet();
    }

    public void workerFinished() {
        activeWorkers.decrementAndGet();
    }

    public void queued(int value) {
        queued.set(value);;
    }

    public long processedCount() {
        return processed.sum();
    }

    public long failedCount() {
        return activeWorkers.get();
    }

    public int activeWorkers() {
        return activeWorkers.get();
    }

    public int queuedItems() {
        return queued.get();
    }
}
