package org.tanchee.inngest.func;

import lombok.experimental.SuperBuilder;

@SuperBuilder
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

    public abstract static class StepOpBuilder<
        C extends StepOp,
        B extends StepOpBuilder<C, B>> {

        public B populateFrom(StepInterruptException e) {
            if (e == null) return self();
            return self()
                .id(e.getHashedId())
                .name(e.getId());
        }
    }
}
