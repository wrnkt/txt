package org.tanchee.inngest.config.serde;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Cancellation {

    @JsonProperty("event")
    private final String event;

    @JsonProperty("if")
    private final String ifExpr;

    @JsonProperty("timeout")
    private final String timeout;

    public Cancellation(String event, String ifExpr, String timeout) {
        this.event = event;
        this.ifExpr = ifExpr;
        this.timeout = timeout;
    }

    public String getEvent()   { return event; }
    public String getIfExpr()  { return ifExpr; }
    public String getTimeout() { return timeout; }
}
