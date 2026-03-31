package org.tanchee.inngest;

public class StepInterruptSleepException extends StepInterruptException {
    public StepInterruptSleepException(String id, String hashedId, Object data) {
        super(id, hashedId, data);
    }
}
