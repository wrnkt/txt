package org.tanchee.inngest;

public class StepInterruptWaitForEventException extends StepInterruptException {
    private final String waitEvent;
    private final String timeout;
    private final String ifExpression;
    public StepInterruptWaitForEventException(
        String id,
        String hashedId,
        String waitEvent,
        String timeout,
        String ifExpression
    ) {
        super(id, hashedId, null);
        this.waitEvent = waitEvent;
        this.timeout = timeout;
        this.ifExpression = ifExpression;

    }
    public String getWaitEvent() {
        return waitEvent;
    }

    public String getTimeout() {
        return timeout;
    }

    public String getIfExpression() {
        return ifExpression;
    }
}
