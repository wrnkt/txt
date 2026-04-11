package org.tanchee.inngest.func;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
class InternalFunctionConfig {

    private final String id;

    @JsonProperty("name")
    private final String name;

    private final List<InngestFunctionTrigger> triggers;

    @JsonProperty("concurrency")
    private final List<Concurrency> concurrency;

    @JsonProperty("throttle")
    private final Throttle throttle;

    @JsonProperty("rateLimit")
    private final RateLimit rateLimit;

    @JsonProperty("debounce")
    private final Debounce debounce;

    @JsonProperty("priority")
    private final Priority priority;

    @JsonProperty("idempotency")
    private final String idempotency;

    @JsonProperty("cancel")
    private final List<Cancellation> cancel;

    @JsonProperty("batchEvents")
    private final BatchEvents batchEvents;

    private final Map<String, StepConfig> steps;

    public InternalFunctionConfig(
            String id,
            String name,
            List<InngestFunctionTrigger> triggers,
            List<Concurrency> concurrency,
            Throttle throttle,
            RateLimit rateLimit,
            Debounce debounce,
            Priority priority,
            String idempotency,
            List<Cancellation> cancel,
            BatchEvents batchEvents,
            Map<String, StepConfig> steps
    ) {
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<InngestFunctionTrigger> getTriggers() {
        return triggers;
    }

    public List<Concurrency> getConcurrency() {
        return concurrency;
    }

    public Throttle getThrottle() {
        return throttle;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public Debounce getDebounce() {
        return debounce;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getIdempotency() {
        return idempotency;
    }

    public List<Cancellation> getCancel() {
        return cancel;
    }

    public BatchEvents getBatchEvents() {
        return batchEvents;
    }

    public Map<String, StepConfig> getSteps() {
        return steps;
    }
}
