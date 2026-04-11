package org.tanchee.inngest.func;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StepError extends RuntimeException {

    private final String name;
    private final String stack;

    public StepError(String message) {
        this(message, "", "");
    }

    public StepError(String message, String name) {
        this(message, name, "");
    }

    public StepError(String message, String name, String stack) {
        super(message);
        this.name = name;
        this.stack = stack;
    }

    public String getName() {
        return name;
    }

    public String getStack() {
        return stack;
    }
}
