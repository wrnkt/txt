package org.tanchee.inngest.func;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Types {
    private Types() {}

    public static class MemoizedRecord extends HashMap<String, Object> {}

    public static class MemoizedState extends HashMap<String, MemoizedRecord> {}
}

public final class InngestEvent {
    private final String name;
    private final Object data;

    public InngestEvent(String name, Object data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public Object getData() {
        return data;
    }
}

public final class SendEventsResponse {
    private final String[] ids;

    public SendEventsResponse(String[] ids) {
        this.ids = ids;
    }

    public String[] getIds() {
        return ids;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SendEventsResponse that)) {
            return false;
        }

        return java.util.Arrays.equals(ids, that.ids);
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(ids);
    }
}

public class StepInvalidStateTypeException extends RuntimeException {
    private final String id;
    private final String hashedId;

    public StepInvalidStateTypeException(String id, String hashedId) {
        super("Step execution interrupted");
        this.id = id;
        this.hashedId = hashedId;
    }

    public String getId() {
        return id;
    }

    public String getHashedId() {
        return hashedId;
    }
}

public class StepInterruptException extends RuntimeException {
    private final String id;
    private final String hashedId;
    private final Object data;

    public StepInterruptException(String id, String hashedId, Object data) {
        super("Interrupt " + id);
        this.id = id;
        this.hashedId = hashedId;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public String getHashedId() {
        return hashedId;
    }

    public Object getData() {
        return data;
    }
}

public class StepInterruptSleepException extends StepInterruptException {
    public StepInterruptSleepException(String id, String hashedId, String data) {
        super(id, hashedId, data);
    }

    @Override
    public String getData() {
        return (String) super.getData();
    }
}

public class StepInterruptSendEventException extends StepInterruptException {
    private final String[] eventIds;

    public StepInterruptSendEventException(String id, String hashedId, String[] eventIds) {
        super(id, hashedId, eventIds);
        this.eventIds = eventIds;
    }

    public String[] getEventIds() {
        return eventIds;
    }
}

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

public class StepInterruptWaitForEventException extends StepInterruptException {
    private final String waitEvent;
    private final String timeout;
    private final String ifExpression;

    public StepInterruptWaitForEventException(
            String id,
            String hashedId,
            String waitEvent,
            String timeout,
            String ifExpression
    ) {
        super(id, hashedId, null);
        this.waitEvent = waitEvent;
        this.timeout = timeout;
        this.ifExpression = ifExpression;
    }

    public String getWaitEvent() {
        return waitEvent;
    }

    public String getTimeout() {
        return timeout;
    }

    public String getIfExpression() {
        return ifExpression;
    }
}

public class StepInterruptErrorException extends StepInterruptException {
    private final Exception error;

    public StepInterruptErrorException(
            String id,
            String hashedId,
            Exception error
    ) {
        super(id, hashedId, null);
        this.error = error;
    }

    public Exception getError() {
        return error;
    }
}

public class Step {
    private final State state;
    private final Inngest client;

    public Step(State state, Inngest client) {
        this.state = state;
        this.client = client;
    }

    public <T> T run(String id, StepFunction<T> fn, Class<T> type) {
        String hashedId = state.getHashFromId(id);

        try {
            return type.cast(state.getState(hashedId, type));
        } catch (StateNotFound e) {
            executeStep(id, hashedId, fn);
        } catch (StepError e) {
            throw e;
        }

        throw new IllegalStateException("step state incorrect type");
    }

    public <T> T invoke(
            String id,
            String appId,
            String fnId,
            Object data,
            String timeout,
            Class<T> type
    ) {
        String hashedId = state.getHashFromId(id);

        try {
            T stepResult = type.cast(state.getState(hashedId, type));

            if (stepResult != null) {
                return stepResult;
            }
        } catch (StateNotFound e) {
            throw new StepInterruptInvokeException(
                    id,
                    hashedId,
                    appId,
                    fnId,
                    data,
                    timeout
            );
        } catch (StepError e) {
            throw e;
        }

        throw new IllegalStateException("step state incorrect type");
    }

    public void sleep(String id, Duration duration) {
        String hashedId = state.getHashFromId(id);

        try {
            Object stepState = state.getState(hashedId);

            if (stepState != null) {
                throw new IllegalStateException(
                        "step state expected sleep, got something else"
                );
            }
        } catch (StateNotFound e) {
            throw new StepInterruptSleepException(
                    id,
                    hashedId,
                    duration.getSeconds() + "s"
            );
        }
    }

    public SendEventsResponse sendEvent(String id, InngestEvent event) {
        return sendEvent(id, new InngestEvent[]{event});
    }

    public SendEventsResponse sendEvent(String id, InngestEvent[] events) {
        String hashedId = state.getHashFromId(id);

        try {
            String[] stepState = state.getState(hashedId, "event_ids", String[].class);

            if (stepState != null) {
                return new SendEventsResponse(stepState);
            }

            throw new IllegalStateException(
                    "step state expected sendEvent, got something else"
            );
        } catch (StateNotFound e) {
            SendEventsResponse response = client.send(events);

            throw new StepInterruptSendEventException(
                    id,
                    hashedId,
                    response.getIds()
            );
        }
    }

    public Object waitForEvent(
            String id,
            String waitEvent,
            String timeout,
            String ifExpression
    ) {
        String hashedId = state.getHashFromId(id);

        try {
            Object stepResult = state.getState(hashedId);

            if (stepResult != null) {
                return stepResult;
            }

            return null;
        } catch (StateNotFound e) {
            throw new StepInterruptWaitForEventException(
                    id,
                    hashedId,
                    waitEvent,
                    timeout,
                    ifExpression
            );
        }
    }

    private <T> void executeStep(
            String id,
            String hashedId,
            StepFunction<T> fn
    ) {
        try {
            T data = fn.execute();
            throw new StepInterruptException(id, hashedId, data);
        } catch (RetryAfterError | NonRetriableError e) {
            throw e;
        } catch (Exception e) {
            throw new StepInterruptErrorException(id, hashedId, e);
        }
    }

    @FunctionalInterface
    public interface StepFunction<T> {
        T execute() throws Exception;
    }
}
