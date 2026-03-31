package org.tanchee.inngest;

public class StepInterruptErrorException extends StepInterruptException {
    private final Exception error;

    public StepInterruptErrorException(
        String id,
        String hashedId,
        Exception error
    ) {
        super(id, hashedId, null);
        this.error = error;
    }
}
