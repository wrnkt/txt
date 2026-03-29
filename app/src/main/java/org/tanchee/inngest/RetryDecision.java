package org.tanchee.inngest;

import java.util.Map;

final class RetryDecision {

    private static final Map.Entry<String, String> NO_RETRY_FALSE =
        Map.entry(InngestHeaderKey.NoRetry.getValue(), "false");

    private final boolean shouldRetry;
    private final Map<String, String> headers;

    public RetryDecision(boolean shouldRetry, Map<String, String> headers) {
        this.shouldRetry = shouldRetry;
        this.headers = headers;
    }

    public boolean shouldRetry() {
        return shouldRetry;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public static RetryDecision fromException(Exception exception) {
        return switch (exception) {
            case RetryAfterError retryAfterError -> new RetryDecision(
                true,
                Map.of(
                    InngestHeaderKey.RetryAfter.getValue(),
                    retryAfterError.getRetryAfter(),
                    NO_RETRY_FALSE.getKey(),
                    NO_RETRY_FALSE.getValue()
                )
            );

            case NonRetriableError ignored -> new RetryDecision(
                false,
                Map.of(InngestHeaderKey.NoRetry.getValue(), "true")
            );

            default -> new RetryDecision(
                true,
                Map.of(NO_RETRY_FALSE.getKey(), NO_RETRY_FALSE.getValue())
            );
        };
    }
}
