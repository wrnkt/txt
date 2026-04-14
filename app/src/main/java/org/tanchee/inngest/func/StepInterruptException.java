package org.tanchee.inngest.func;

import lombok.Builder;

@Builder
public class StepInterruptException extends RuntimeException {
    private final String id;
    private final String hashedId;

    //NOTE: originally Any? use Optional?
    private final Object data;

    public StepInterruptException(String id, String hashedId, Object data) {
        super("Interrupt " + id);
        this.id = id;
        this.hashedId = hashedId;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public String getHashedId() {
        return hashedId;
    }

    public Object getData() {
        return data;
    }

}
