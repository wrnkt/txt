package org.tanchee.inngest;

public class StepInterruptSendEventException extends StepInterruptException {
    private final String[] eventIds;

    public StepInterruptSendEventException(
        String id,
        String hashedId,
        Object data,
        String... eventIds
    ) {
        super(id, hashedId, data);
        this.eventIds = eventIds;
    }

    public String[] getEventIds() {
        return eventIds;
    }
}
