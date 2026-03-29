package org.tanchee.inngest;

public enum InngestSystem {
    EventKey("INNGEST_EVENT_KEY"),
    SigningKey("INNGEST_SIGNING_KEY"),
    Env("INNGEST_ENV"),

    EventApiBaseUrl("INNGEST_BASE_URL"),
    ApiBaseUrl("INNGEST_API_BASE_URL"),
    LogLevel("INNGEST_LOG_LEVEL"),

    ApiOrigin("INNGEST_API_ORIGIN"),
    ServePath("INNGEST_SERVE_PATH"),
    Dev("INNGEST_DEV");

    public final String value;

    private InngestSystem(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
