package org.tanchee.inngest.config;

import java.util.HashMap;
import java.util.Map;

public class StepConfig {

    private String id = "";
    private String name = "";
    private Map<String, Integer> retries;
    private Map<String, String> runtime = new HashMap<>();

    public StepConfig(String id, String name, Map<String, Integer> retries) {
        this.id = id;
        this.name = name;
        this.retries = retries;
        this.runtime.put("type", "http");
    }

    public StepConfig(String id, String name, Map<String, Integer> retries, Map<String, String> runtime) {
        this.id = id;
        this.name = name;
        this.retries = retries;
        this.runtime = runtime;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getRetries() {
        return retries;
    }

    public Map<String, String> getRuntime() {
        return runtime;
    }

}
