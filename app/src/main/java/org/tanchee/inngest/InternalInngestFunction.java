package org.tanchee.inngest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.tanchee.inngest.config.InngestFunctionConfigBuilder;
import org.tanchee.inngest.config.InternalFunctionConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class InternalInngestFunction implements Function {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final InngestFunctionConfigBuilder configBuilder;
    private final BiFunction<FunctionContext, Step, Object> handler;

    public InternalInngestFunction(
            InngestFunctionConfigBuilder configBuilder,
            BiFunction<FunctionContext, Step, Object> handler) {
        this.configBuilder = configBuilder;
        this.handler       = handler;
    }

    public String id() {
        return configBuilder.id();
    }

    public StepOp call(FunctionContext ctx, Inngest client, String requestBody) {
        State state = new State(requestBody);
        Step step   = new Step(state, client);

        try {
            Object data = handler.apply(ctx, step);
            return new StepResult("", "", OpCode.StepRun, ResultStatusCode.FunctionComplete, data);

        } catch (StepInterruptSendEventException e) {
            return new StepResult(
                    e.getHashedId(),
                    e.getId(),
                    OpCode.Step,
                    ResultStatusCode.StepComplete,
                    new SendEventPayload(e.getEventIds()));

        } catch (StepInterruptWaitForEventException e) {
            Map<String, String> opts = new LinkedHashMap<>();
            opts.put("event", e.getWaitEvent());
            opts.put("timeout", e.getTimeout());
            if (e.getIfExpression() != null) {
                opts.put("if", e.getIfExpression());
            }
            return new StepOptions(
                    e.getHashedId(),
                    e.getId(),
                    OpCode.WaitForEvent,
                    ResultStatusCode.StepComplete,
                    opts);

        } catch (StepInterruptSleepException e) {
            Map<String, String> opts = new HashMap<>();
            // WARN: hack, not sure if this will work. original below
            // opts.put("duration", e.getData());
            opts.put("duration", e.getData().toString());
            return new StepOptions(
                    e.getHashedId(),
                    e.getId(),
                    OpCode.Sleep,
                    ResultStatusCode.StepComplete,
                    opts);

        } catch (StepInterruptInvokeException e) {
            String functionId = String.format("%s-%s", e.getAppId(), e.getFnId());
            Map<String, Object> opts = new LinkedHashMap<>();
            opts.put("function_id", functionId);
            opts.put("payload", Map.of("data", e.getData()));
            if (e.getTimeout() != null) {
                opts.put("timeout", e.getTimeout());
            }
            return new StepOptionsInvoke(
                    e.getHashedId(),
                    e.getId(),
                    OpCode.InvokeFunction,
                    ResultStatusCode.StepComplete,
                    opts);

        } catch (StepInterruptErrorException e) {
            return new StepResult(
                    e.getHashedId(),
                    e.getId(),
                    OpCode.StepError,
                    ResultStatusCode.StepError,
                    null,
                    e);

        } catch (StepInterruptException e) {
            return new StepResult(
                    e.getHashedId(),
                    e.getId(),
                    OpCode.StepRun,
                    ResultStatusCode.StepComplete,
                    serializeStepData(e.getData()));

        } catch (StepInvalidStateTypeException e) {
            return new StepResult(
                    e.getHashedId(),
                    e.getId(),
                    OpCode.StepStateFailed,
                    ResultStatusCode.RetriableError);
        }
    }

    public InternalFunctionConfig getFunctionConfig(String serveUrl, Inngest client) {
        return configBuilder.build(client.appId(), serveUrl);
    }

    private JsonNode serializeStepData(Object stepData) {
        if (stepData == null) {
            return null;
        }

        try {
            String jsonString     = MAPPER.writeValueAsString(stepData);
            JsonNode readOnlyJson = MAPPER.readTree(jsonString);

            if (!readOnlyJson.isObject()) {
                // primitives can be serialized directly
                return readOnlyJson;
            }

            ObjectNode writableJson = (ObjectNode) MAPPER.readTree(jsonString);
            writableJson.put("class", stepData.getClass().getName());
            return writableJson;

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize step data", e);
        }
    }
}
