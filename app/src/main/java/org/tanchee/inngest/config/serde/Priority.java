package org.tanchee.inngest.config.serde;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Priority {

    @JsonProperty("run")
    private final String run;

    public Priority(String run) {
        this.run = run;
    }

    public String getRun() { return run; }
}
