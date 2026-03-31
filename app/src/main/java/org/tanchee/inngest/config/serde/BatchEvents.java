package org.tanchee.inngest.config.serde;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchEvents {

    @JsonProperty("maxSize")
    private final int maxSize;

    @JsonProperty("timeout")
    @JsonSerialize(using = DurationSerializer.class)
    private final Duration timeout;

    @JsonProperty("key")
    private final String key;

    public BatchEvents(int maxSize, Duration timeout, String key) {
        this.maxSize = maxSize;
        this.timeout = timeout;
        this.key = key;
    }

    public int getMaxSize()     { return maxSize; }
    public Duration getTimeout() { return timeout; }
    public String getKey()      { return key; }
}
