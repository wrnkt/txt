package org.tanchee.inngest.func;

public enum ResultStatusCode {
    StepComplete(206, "Step complete"),
    StepError(206, "Step error"),
    FunctionComplete(200, "Function complete"),
    NonRetriableError(400, "Bad request"),
    RetriableError(500, "Function error");

    private final int code;
    private final String msg;

    private ResultStatusCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int code() {
        return code;
    }

    public String message() {
        return msg;
    }
}
