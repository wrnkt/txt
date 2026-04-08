package org.tanchee.inngest.config;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tanchee.inngest.config.serde.BatchEvents;
import org.tanchee.inngest.config.serde.Cancellation;
import org.tanchee.inngest.config.serde.Concurrency;
import org.tanchee.inngest.config.serde.ConcurrencyScope;
import org.tanchee.inngest.config.serde.Debounce;
import org.tanchee.inngest.config.serde.Priority;
import org.tanchee.inngest.config.serde.RateLimit;
import org.tanchee.inngest.config.serde.Throttle;

import org.tanchee.inngest.InngestFunctionTrigger;
import org.tanchee.inngest.InngestFunctionTriggers;

public class InngestFunctionConfigBuilder {

    private String id;
    String name;
    private final List<InngestFunctionTrigger> triggers = new ArrayList<>();
    private List<Concurrency> concurrency;
    private int retries = 3;
    private Throttle throttle;
    private RateLimit rateLimit;
    private Debounce debounce;
    private Priority priority;
    private String idempotency;
    private List<Cancellation> cancel;
    private BatchEvents batchEvents;

    /** @param id A unique identifier for the function that should not change over time */
    public InngestFunctionConfigBuilder id(String id) {
        this.id = id;
        return this;
    }

    public String id() {
        return id;
    }

    /** @param name A formatted name for the function, visible in UIs and logs */
    public InngestFunctionConfigBuilder name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return this.name;
    }

    /**
     * Define a function trigger using a given InngestFunctionTrigger instance.
     *
     * @param trigger An event or cron function trigger
     */
    public InngestFunctionConfigBuilder trigger(InngestFunctionTrigger trigger) {
        this.triggers.add(trigger);
        return this;
    }

    /**
     * Define a function trigger for any matching events with a given name.
     *
     * @param event The name of the event to trigger on
     */
    public InngestFunctionConfigBuilder triggerEvent(String event) {
        this.triggers.add(new InngestFunctionTriggers.Event(event, null));
        return this;
    }

    /**
     * Define a function trigger for any matching events with a given name and
     * an optional CEL expression filter.
     *
     * @param event   The name of the event to trigger on
     * @param ifExpr  A CEL expression to filter matching events. Example: "event.data.appId == '12345'"
     */
    public InngestFunctionConfigBuilder triggerEventIf(String event, String ifExpr) {
        this.triggers.add(new InngestFunctionTriggers.Event(event, ifExpr));
        return this;
    }

    /** @param cron A crontab expression */
    public InngestFunctionConfigBuilder triggerCron(String cron) {
        this.triggers.add(new InngestFunctionTriggers.Cron(cron));
        return this;
    }

    /**
     * Define events that can cancel a running or sleeping function.
     * Uses a Duration-based timeout.
     *
     * @param event   The name of the event that should cancel the function run
     * @param ifExpr  CEL expression that must evaluate to true to cancel the run (nullable)
     * @param timeout Duration after which the cancel is no longer valid (nullable)
     */
    public InngestFunctionConfigBuilder cancelOn(String event, String ifExpr, Duration timeout) {
        String timeoutStr = (timeout != null)
                ? timeout.getSeconds() + "s"
                : null;
        return cancelOn(new Cancellation(event, ifExpr, timeoutStr));
    }

    /**
     * Define events that can cancel a running or sleeping function.
     * Uses an Instant-based timeout.
     *
     * @param event   The name of the event that should cancel the function run
     * @param ifExpr  CEL expression that must evaluate to true to cancel the run (nullable)
     * @param timeout Instant until which the cancel is valid (nullable)
     */
    public InngestFunctionConfigBuilder cancelOn(String event, String ifExpr, Instant timeout) {
        String timeoutStr = (timeout != null) ? timeout.toString() : null;
        return cancelOn(new Cancellation(event, ifExpr, timeoutStr));
    }

    /** Convenience overload — no timeout. */
    public InngestFunctionConfigBuilder cancelOn(String event, String ifExpr) {
        return cancelOn(event, ifExpr, (Duration) null);
    }

    /** Convenience overload — no ifExpr or timeout. */
    public InngestFunctionConfigBuilder cancelOn(String event) {
        return cancelOn(event, null, (Duration) null);
    }

    InngestFunctionConfigBuilder cancelOn(Cancellation cancellation) {
        if (this.cancel == null) {
            this.cancel = new ArrayList<>();
        }
        this.cancel.add(cancellation);
        return this;
    }

    /**
     * Configure batch event processing.
     *
     * @param maxSize The maximum number of events to execute the function with
     * @param timeout The maximum duration of time to wait before executing the function
     * @param key     A CEL expression to group event batches by (nullable)
     */
    public InngestFunctionConfigBuilder batchEvents(int maxSize, Duration timeout, String key) {
        this.batchEvents = new BatchEvents(maxSize, timeout, key);
        return this;
    }

    /** Convenience overload — no key. */
    public InngestFunctionConfigBuilder batchEvents(int maxSize, Duration timeout) {
        return batchEvents(maxSize, timeout, null);
    }

    /**
     * Configure step concurrency limit.
     *
     * @param limit Maximum number of concurrently executing steps
     * @param key   A CEL expression to apply the limit using event payload properties (nullable)
     * @param scope The scope to apply the limit to (nullable)
     */
    public InngestFunctionConfigBuilder concurrency(int limit, String key, ConcurrencyScope scope) {
        if (scope == ConcurrencyScope.ENVIRONMENT && key == null) {
            throw new InngestInvalidConfigurationException(
                    "Concurrency key required with environment scope");
        }
        if (scope == ConcurrencyScope.ACCOUNT && key == null) {
            throw new InngestInvalidConfigurationException(
                    "Concurrency key required with account scope");
        }

        Concurrency c = new Concurrency(limit, key, scope);
        if (this.concurrency == null) {
            this.concurrency = new ArrayList<>();
        } else if (this.concurrency.size() == 2) {
            throw new InngestInvalidConfigurationException(
                    "Maximum of 2 concurrency options allowed");
        }
        this.concurrency.add(c);
        return this;
    }

    /** Convenience overload — no key or scope. */
    public InngestFunctionConfigBuilder concurrency(int limit) {
        return concurrency(limit, null, null);
    }

    /**
     * Specifies the maximum number of retries for all steps across this function.
     *
     * @param attempts The number of times to retry a step before failing. Defaults to 3.
     */
    public InngestFunctionConfigBuilder retries(int attempts) {
        this.retries = attempts;
        return this;
    }

    /**
     * Configure function throttle limit.
     *
     * @param limit  Total runs allowed to start within the given period
     * @param period Period of time for the rate limit
     * @param key    Optional expression for a throttling key (nullable)
     * @param burst  Runs allowed to start in a single burst (nullable)
     */
    public InngestFunctionConfigBuilder throttle(int limit, Duration period, String key, Integer burst) {
        this.throttle = new Throttle(limit, period, key, burst);
        return this;
    }

    /** Convenience overload — no key or burst. */
    public InngestFunctionConfigBuilder throttle(int limit, Duration period) {
        return throttle(limit, period, null, null);
    }

    /**
     * Configure function rate limit.
     *
     * @param limit  The number of times to allow the function to run per period
     * @param period The period of time to allow the function to run {@code limit} times
     * @param key    An optional expression for rate limiting (nullable)
     */
    public InngestFunctionConfigBuilder rateLimit(int limit, Duration period, String key) {
        this.rateLimit = new RateLimit(limit, period, key);
        return this;
    }

    /** Convenience overload — no key. */
    public InngestFunctionConfigBuilder rateLimit(int limit, Duration period) {
        return rateLimit(limit, period, null);
    }

    /**
     * Debounce delays function execution until {@code period} has elapsed since the last trigger.
     *
     * @param period  Delay after receiving the last trigger before running the function
     * @param key     Optional key to use for debouncing (nullable)
     * @param timeout Maximum time a debounce can be extended before the function must run (nullable)
     */
    public InngestFunctionConfigBuilder debounce(Duration period, String key, Duration timeout) {
        this.debounce = new Debounce(period, key, timeout);
        return this;
    }

    /** Convenience overload — no key or timeout. */
    public InngestFunctionConfigBuilder debounce(Duration period) {
        return debounce(period, null, null);
    }

    /**
     * Configure run priority.
     *
     * @param run A CEL expression returning a number between -600 and 600 to determine
     *            execution priority relative to other enqueued runs
     */
    public InngestFunctionConfigBuilder priority(String run) {
        this.priority = new org.tanchee.inngest.config.serde.Priority(run);
        return this;
    }

    /**
     * Specify an idempotency key using event data. If specified, this overrides
     * the {@code rateLimit} configuration.
     *
     * @param idempotencyKey A CEL expression producing a unique string key
     */
    public InngestFunctionConfigBuilder idempotency(String idempotencyKey) {
        this.idempotency = idempotencyKey;
        return this;
    }

    private Map<String, StepConfig> buildSteps(String serveUrl) {
        String scheme = serveUrl.split("://")[0];
        Map<String, String> runtime = new HashMap<>();
        runtime.put("type", scheme);
        runtime.put("url", serveUrl + "?fnId=" + id + "&stepId=step");

        Map<String, Integer> retriesMap = new HashMap<>();
        retriesMap.put("attempts", this.retries);

        StepConfig stepConfig = new StepConfig("step", "step", retriesMap, runtime);

        Map<String, StepConfig> steps = new HashMap<>();
        steps.put("step", stepConfig);
        return steps;
    }

    public InternalFunctionConfig build(String appId, String serverUrl) {
        if (id == null) {
            throw new InngestInvalidConfigurationException(
                    "Function id must be configured via builder");
        }
        String globalId = String.format("%s-%s", appId, id);
        return new InternalFunctionConfig(
                globalId,
                name != null ? name : id,
                triggers,
                concurrency,
                throttle,
                rateLimit,
                debounce,
                priority,
                idempotency,
                cancel,
                batchEvents,
                buildSteps(serverUrl)
        );
    }
}
