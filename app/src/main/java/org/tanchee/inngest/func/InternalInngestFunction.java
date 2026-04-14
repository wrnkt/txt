package org.tanchee.inngest.func;

import static org.tanchee.common.Functional.mapOf;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.tanchee.inngest.Inngest;
import org.tanchee.inngest.SendEventPayload;
import org.tanchee.inngest.State;
import org.tanchee.inngest.config.InngestFunctionConfigBuilder;
import org.tanchee.inngest.config.InternalFunctionConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class InternalInngestFunction implements Function {
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
            return StepResult.builder()
                .data(data)
                .id("")
                .name("")
                .opCode(OpCode.StepRun)
                .statusCode(ResultStatusCode.FunctionComplete)
                .build();
        } catch (StepInterruptSendEventException e) {
            return StepResult.builder()
                .id(e.getHashedId())
                .name(e.getId())
                .opCode(OpCode.Step)
                .statusCode(ResultStatusCode.StepComplete)
                .error(null)
                .data(new SendEventPayload(e.getEventIds()))
                .build();
        } catch (StepInterruptWaitForEventException e) {
            return StepOptions.builder()
                .id(e.getHashedId())
                .name(e.getId())
                .opCode(OpCode.WaitForEvent)
                .statusCode(ResultStatusCode.StepComplete)
                .opts(mapOf(opts -> {
                    opts.put("event", e.getWaitEvent());
                    opts.put("timeout", e.getTimeout());
                    if (e.getIfExpression() != null) {
                        opts.put("if", e.getIfExpression());
                    }
                }))
                .build();
        } catch (StepInterruptSleepException e) {
            return StepOptions.builder()
                .id(e.getHashedId())
                .name(e.getId())
                .opCode(OpCode.Sleep)
                .statusCode(ResultStatusCode.StepComplete)
                .opts(mapOf(opts -> {
                    // WARN: hack, not sure if this will work. original below
                    // opts.put("duration", e.getData());
                    opts.put("duration", e.getData().toString());
                }))
                .build();

        } catch (StepInterruptInvokeException e) {
            String functionId = String.format("%s-%s", e.getAppId(), e.getFnId());
            return StepOptions.builder()
                .id(e.getHashedId())
                .name(e.getId())
                .opCode(OpCode.InvokeFunction)
                .statusCode(ResultStatusCode.StepComplete)
                .opts(mapOf(opts -> {
                    opts.put("function_id", functionId);

                    Map<String, Object> payload = new HashMap<>();
                    payload.put("data", e.getData());
                    opts.put("payload", payload);

                    if (e.getTimeout() != null) {
                        opts.put("timeout", e.getTimeout());
                    }
                }))
                .build();
        } catch (StepInterruptErrorException e) {
            if (e instanceof StepInterruptErrorException siee) {
                return StepResult.builder()
                    .id(e.getHashedId())
                    .name(e.getId())
                    .opCode(OpCode.StepError)
                    .statusCode(ResultStatusCode.StepError)
                    .error(siee.getError())
                    .build();
            } else {
                // WARN: NOT GOOD, this is a placeholder.
                //       the entire tree should probably be done
                //       with one catch and these instanceof checks.
                throw new RuntimeException(e);
            }
        } catch (StepInterruptException e) {
            // NOTE - Currently this error could be caught in the user's own function
            // that wraps a
            // step.run() - how can we prevent that or warn?eturn new StepResult(
            return StepResult.builder()
                .id(e.getHashedId())
                .name(e.getId())
                .opCode(OpCode.StepRun)
                .statusCode(ResultStatusCode.StepComplete)
                .data(serializeStepData(e.getData()))
                .build();
        } catch (StepInvalidStateTypeException e) {
            // TODO - handle with the proper OpCode
            return StepResult.builder()
                .id(e.getHashedId())
                .name(e.getId())
                .opCode(OpCode.StepStateFailed)
                .statusCode(ResultStatusCode.RetriableError)
                .data(null)
                .build();
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
