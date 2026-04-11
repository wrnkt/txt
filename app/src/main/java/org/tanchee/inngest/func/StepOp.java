package org.tanchee.inngest.func;

public abstract class StepOp {
    protected String id = "";
    protected String name = "";
    protected OpCode opCode;
    protected ResultStatusCode statusCode;

    public StepOp(
        String id, String name, OpCode opCode, ResultStatusCode statusCode
    ) {
        this.id = id;
        this.name = name;
        this.opCode = opCode;
        this.statusCode = statusCode;
    }

    public String id() {
        return id;
    }
    public String name() {
        return name;
    }
    public OpCode opCode() {
        return opCode;
    }
    public ResultStatusCode statusCode() {
        return statusCode;
    }
}
