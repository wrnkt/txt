package org.tanchee.inngest.func;

import org.tanchee.inngest.SendEventPayload;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class StepResult extends StepOp {
    private Object data;
    private Exception error;

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

    public abstract static class StepResultBuilder<
        C extends StepResult,
        B extends StepResultBuilder<C, B>>
        extends StepOpBuilder<C, B> {

        public B populateFrom(StepInterruptException e) {
            if (e == null) return self();
            return self()
                .id(e.getHashedId())
                .name(e.getId())
                .data(e.getData());
        }

        public B populateFrom(StepInterruptSendEventException e) {
            if (e == null) return self();
            return self()
                .id(e.getHashedId())
                .name(e.getId())
                .data(new SendEventPayload(e.getEventIds()));
        }
    }

}
