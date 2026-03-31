package org.tanchee.inngest;

public class StepResult extends StepOp {
    private Object data = null;
    private Exception error = null;

    public StepResult(String id, String name, OpCode opCode, ResultStatusCode statusCode) {
        super(id, name, opCode, statusCode);
    }

    public StepResult(String id, String name, OpCode opCode, ResultStatusCode statusCode, Object data) {
        super(id, name, opCode, statusCode);
        this.data = data;
    }

    public StepResult(String id, String name, OpCode opCode, ResultStatusCode statusCode, Object data,
            Exception error) {
        super(id, name, opCode, statusCode);
        this.data = data;
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public OpCode getOpCode() {
        return opCode;
    }

    public ResultStatusCode getStatusCode() {
        return statusCode;
    }

    public Object getData() {
        return data;
    }

    public Exception getError() {
        return error;
    }

}
