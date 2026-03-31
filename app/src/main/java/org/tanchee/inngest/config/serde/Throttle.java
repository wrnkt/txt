package org.tanchee.inngest.config.serde;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Throttle {

    @JsonProperty("limit")
    private final int limit;

    @JsonProperty("period")
    @JsonSerialize(using = DurationSerializer.class)
    private final Duration period;

    @JsonProperty("key")
    private final String key;

    @JsonProperty("burst")
    private final Integer burst;

    public Throttle(int limit, Duration period, String key, Integer burst) {
        this.limit = limit;
        this.period = period;
        this.key = key;
        this.burst = burst;
    }

    public int getLimit()       { return limit; }
    public Duration getPeriod() { return period; }
    public String getKey()      { return key; }
    public Integer getBurst()   { return burst; }
}
