package org.tanchee.inngest;

public class StepInterruptInvokeException extends StepInterruptException {
    private final String appId;
    private final String fnId;
    private final String timeout;

    public StepInterruptInvokeException(
        String id,
        String hashedId,
        String appId,
        String fnId,
        Object data,
        String timeout
    ) {
        super(id, hashedId, data);
        this.appId = appId;
        this.fnId = fnId;
        this.timeout = timeout;
    }

    public String getAppId() {
        return appId;
    }

    public String getFnId() {
        return fnId;
    }

    public String getTimeout() {
        return timeout;
    }

}
