package org.tanchee.inngest;

public class NonRetriableError extends RuntimeException {
    public NonRetriableError(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
