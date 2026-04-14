package org.tanchee.inngest.func;

import java.util.Map;

public class StepOptionsInvoke extends StepOp {
    private Map<String, Object> opts;

    public StepOptionsInvoke(String id, String name, OpCode opCode, ResultStatusCode statusCode, Map<String, Object> opts) {
        super(id, name, opCode, statusCode);
        this.opts = opts;
    }

    public Map<String, Object> getOpts() {
        return opts;
    }
}
