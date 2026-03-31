package org.tanchee.inngest.config.serde;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Concurrency {

    @JsonProperty("limit")
    private final int limit;

    @JsonProperty("key")
    private final String key;

    @JsonProperty("scope")
    private final ConcurrencyScope scope;

    public Concurrency(int limit, String key, ConcurrencyScope scope) {
        this.limit = limit;
        this.key = key;
        this.scope = scope;
    }

    public int getLimit()              { return limit; }
    public String getKey()             { return key;   }
    public ConcurrencyScope getScope() { return scope; }
}
