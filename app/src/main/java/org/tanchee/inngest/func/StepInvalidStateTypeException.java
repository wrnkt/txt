package org.tanchee.inngest.func;

// NOTE: this is originally Kotlin Throwable
public class StepInvalidStateTypeException extends RuntimeException {
    private static final String MSG = "Step execution interrupted";
    private final String id;
    private final String hashedId;

    public StepInvalidStateTypeException(String id, String hashedId) {
        super(MSG);
        this.id = id;
        this.hashedId = hashedId;
    }

    public static String getMsg() {
        return MSG;
    }

    public String getId() {
        return id;
    }

    public String getHashedId() {
        return hashedId;
    }

}
