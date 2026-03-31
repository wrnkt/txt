package org.tanchee.inngest.config;

import java.util.List;
import java.util.Map;

import org.tanchee.inngest.InngestFunctionTrigger;
import org.tanchee.inngest.config.serde.Concurrency;
import org.tanchee.inngest.config.serde.Throttle;
import org.tanchee.inngest.config.serde.RateLimit;
import org.tanchee.inngest.config.serde.Debounce;
import org.tanchee.inngest.config.serde.Priority;
import org.tanchee.inngest.config.serde.Cancellation;
import org.tanchee.inngest.config.serde.BatchEvents;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternalFunctionConfig {
    private String id;
    private String name;
    private List<InngestFunctionTrigger> triggers;
    private List<Concurrency> concurrency;
    private Throttle throttle;
    private RateLimit rateLimit;
    private Debounce debounce;
    private Priority priority;
    private String idempotency;
    private List<Cancellation> cancel;
    private BatchEvents batchEvents;
    private Map<String, StepConfig> steps;

    public InternalFunctionConfig(String id, String name, List<InngestFunctionTrigger> triggers, List<Concurrency> concurrency,
                                  Throttle throttle, RateLimit rateLimit, Debounce debounce, Priority priority,
                                  String idempotency, List<Cancellation> cancel, BatchEvents batchEvents,
                                  Map<String, StepConfig> steps) {
        this.id = id;
        this.name = name;
        this.triggers = triggers;
        this.concurrency = concurrency;
        this.throttle = throttle;
        this.rateLimit = rateLimit;
        this.debounce = debounce;
        this.priority = priority;
        this.idempotency = idempotency;
        this.cancel = cancel;
        this.batchEvents = batchEvents;
        this.steps = steps;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<InngestFunctionTrigger> getTriggers() { return triggers; }
    public List<Concurrency> getConcurrency() { return concurrency; }
    public Throttle getThrottle() { return throttle; }
    public RateLimit getRateLimit() { return rateLimit; }
    public Debounce getDebounce() { return debounce; }
    public Priority getPriority() { return priority; }
    public String getIdempotency() { return idempotency; }
    public List<Cancellation> getCancel() { return cancel; }
    public BatchEvents getBatchEvents() { return batchEvents; }
    public Map<String, StepConfig> getSteps() { return steps; }
}
