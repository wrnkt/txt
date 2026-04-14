package org.tanchee.inngest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class InngestFunctionTrigger {

    private final String event;

    @JsonProperty("expression")
    private final String ifExpression;

    private final String cron;

    protected InngestFunctionTrigger(
        String event,
        String ifExpression,
        String cron
    ) {
        this.event = event;
        this.ifExpression = ifExpression;
        this.cron = cron;
    }

    protected InngestFunctionTrigger() {
        this(null, null, null);
    }

    public String getEvent() {
        return event;
    }

    public String getIfExpression() {
        return ifExpression;
    }

    public String getCron() {
        return cron;
    }
}
