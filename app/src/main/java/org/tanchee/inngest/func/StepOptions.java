package org.tanchee.inngest.func;

import java.util.Map;
import java.util.Objects;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class StepOptions extends StepOp {
    private final Map<String, Object> opts;

    public StepOptions(String id, String name, OpCode opCode, ResultStatusCode statusCode, Map<String, Object> opts) {
        super(id, name, opCode, statusCode);
        this.opts = opts;
    }

    public Map<String, Object> getOpts() {
        return opts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepOptions)) return false;
        StepOptions that = (StepOptions) o;
        return Objects.equals(id, that.id)     && 
               Objects.equals(name, that.name) &&
               opCode == that.opCode           &&
               statusCode == that.statusCode   &&
               Objects.equals(opts, that.opts);
      }

      @Override
      public int hashCode() { return Objects.hash(id, name, opCode, statusCode, opts); }

      @Override
      public String toString() {
          return "StepOptions(id=" + id + ", name=" + name + ", opCode=" + opCode +
                 ", statusCode=" + statusCode + ", opts=" + opts + ")";
      }
  }

