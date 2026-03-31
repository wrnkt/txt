package org.tanchee.inngest.config.serde;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ConcurrencyScope {
    ACCOUNT("account"),
    ENVIRONMENT("env"),
    FUNCTION("fn");

    private final String value;

    ConcurrencyScope(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
