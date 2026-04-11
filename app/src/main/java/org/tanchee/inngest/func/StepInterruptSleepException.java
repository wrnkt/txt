package org.tanchee.inngest.func;

public class StepInterruptSleepException extends StepInterruptException {
    public StepInterruptSleepException(String id, String hashedId, Object data) {
        super(id, hashedId, data);
    }
}
