package org.tanchee.inngest.http;

import java.util.Map;

public class RequestConfig {
    private final Map<String, String> headers;

    public RequestConfig(Map<String, String> headers) {
        this.headers = headers;
    }

    private RequestConfig() {
        this(null);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

}
