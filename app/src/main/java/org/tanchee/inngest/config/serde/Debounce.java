package org.tanchee.inngest.config.serde;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Debounce {

    @JsonProperty("period")
    @JsonSerialize(using = DurationSerializer.class)
    private final Duration period;

    @JsonProperty("key")
    private final String key;

    @JsonProperty("timeout")
    @JsonSerialize(using = DurationSerializer.class)
    private final Duration timeout;

    public Debounce(Duration period, String key, Duration timeout) {
        this.period = period;
        this.key = key;
        this.timeout = timeout;
    }

    public Duration getPeriod()  { return period; }
    public String getKey()       { return key; }
    public Duration getTimeout() { return timeout; }
}
