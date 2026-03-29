package org.tanchee.inngest;

public enum InngestHeaderKey {
    ContentType("contentType"),
    UserAgent("user-agent"),
    Sdk("x-inngest-sdk"),
    Framework("x-inngest-framework"),
    Environment("x-inngest-environment"),
    Platform("x-inngest-platform"),
    NoRetry("x-inngest-no-retry"),
    RequestVersion("x-inngest-req-version"),
    RetryAfter("retry-after"),
    ServerKind("x-inngest-server-kind"),
    ExpectedServerKind("x-inngest-expected-server-kind"),
    Signature("x-inngest-signature");

    private final String value;

    private InngestHeaderKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
