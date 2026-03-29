package org.tanchee.inngest;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class RetryAfterError extends RuntimeException {

    private final String retryAfter;

    public RetryAfterError(String message, Object retryAfter) {
        this(message, retryAfter, null);
    }

    public RetryAfterError(String message, Object retryAfter, Throwable cause) {
        super(message, cause);
        this.retryAfter = switch (retryAfter) {
            case ZonedDateTime zdt ->
                zdt.format(DateTimeFormatter.ISO_INSTANT);

            case Integer seconds ->
                Integer.toString(seconds / 1000);

            case String value ->
                Integer.toString(Integer.parseInt(value) / 1000);

            case null ->
                throw new IllegalArgumentException("retryAfter cannot be null");

            default ->
                throw new IllegalArgumentException(
                    "Invalid retryAfter type: " + retryAfter.getClass().getSimpleName()
                );
        };
    }

    public String getRetryAfter() {
        return retryAfter;
    }
}
