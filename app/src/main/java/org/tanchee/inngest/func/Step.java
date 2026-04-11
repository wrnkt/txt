package org.tanchee.inngest.func;

import java.time.Duration;
import java.util.function.Supplier;

import org.tanchee.inngest.State;
import org.tanchee.inngest.Inngest;
import org.tanchee.inngest.State.StateNotFound;

public class Step {
    private final State state;
    private final Inngest client;

    public Step(State state, Inngest client) {
        this.state = state;
        this.client = client;
    }

    /**
     * Run a function as a step.
     *
     * @param id   Unique step id for memoization
     * @param fn   The function to run
     * @param type The expected return type of the function
     * @throws StepError if the function throws an Exception
     */
    public <T> T run(String id, Supplier<T> fn, Class<T> type) {
        String hashedId = state.getHashFromId(id);

        try {
            return state.getState(hashedId, type);
        } catch (StateNotFound snf) {
            // NOTE: no existing step, execute the step
            executeStep(id, hashedId, fn);
        } catch (StepError se) {
            throw se;
        }

        // TODO: handle inwardly stored step types properly
        throw new RuntimeException("step state incorrect type");
    }

    private <T> void executeStep(String id, String hashedId, Supplier<T> fn) {
        try {
            T data = fn.get();
            throw new StepInterruptException(id, hashedId, data);
        } catch (RetryAfterError | NonRetriableError e) {
            throw e;
        } catch (Exception ex) {
            throw new StepInterruptErrorException(id, hashedId, ex);
        }
    }

    /**
     * Invoke another Inngest function as a step.
     *
     * @param id      Unique step id for memoization
     * @param appId   ID of the Inngest app which contains the function to invoke
     * @param fnId    ID of the function to invoke
     * @param data    Data to pass within {@code event.data} to the function
     * @param timeout Optional timeout for the invoked function; if the invoked function does not
     *                finish within this time it will be marked as failed (nullable)
     * @param type    The expected return type of the invoked function
     * @throws StepError if the invoked function fails
     */
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
            T stepResult = state.getState(hashedId, type);
            if (null != stepResult) {
                return stepResult;
            }
        } catch (StateNotFound snfe) {
            throw new StepInterruptInvokeException(id, hashedId, appId, fnId, data, timeout);
        } catch (StepError se) {
            throw se;
        }

        // TODO - handle invalidly stored step types properly
        throw new RuntimeException("step state incorrect type");
    }

    /** Convenience overload — no timeout. */
    public <T> T invoke(String id, String appId, String fnId, Object data, Class<T> type) {
        return invoke(id, appId, fnId, data, null, type);
    }

    /**
     * Sleep for a specific duration.
     *
     * @param id       Unique step id for memoization
     * @param duration The duration of time to sleep for
     */
    public void sleep(String id, Duration duration) {
        String hashedId = state.getHashFromId(id);

        try {
            Object stepState = state.getState(hashedId, Object.class);
            if (stepState != null) {
                throw new RuntimeException("step state expected sleep, got something else");
            }
            // null result is the expected completed-sleep state — return normally
        } catch (StateNotFound e) {
            throw new StepInterruptSleepException(id, hashedId, duration.getSeconds() + "s");
        }
    }

    /**
     * Sends a single event to Inngest as a step.
     *
     * @param id    Unique step id for memoization
     * @param event The event to send
     */
    public SendEventsResponse sendEvent(String id, InngestEvent event) {
        return sendEvent(id, new InngestEvent[]{ event });
    }

    /**
     * Sends multiple events to Inngest as a step.
     *
     * @param id     Unique step id for memoization
     * @param events The events to send
     */
    public SendEventsResponse sendEvent(String id, InngestEvent[] events) {
        String hashedId = state.getHashFromId(id);

        try {
            String[] stepState = state.getState(hashedId, String[].class, "event_ids");
            if (stepState != null) {
                return new SendEventsResponse(stepState);
            }
            throw new RuntimeException("step state expected sendEvent, got something else");
        } catch (StateNotFound e) {
            // FIX: not sure what's expected here. kt is not very clear
            // on what the intended exception behavior is
            SendEventsResponse response = client.send(events);
            throw new StepInterruptSendEventException(id, hashedId, response.getIds());
        }
    }

    /**
     * Waits for a matching event, optionally filtered by a CEL expression.
     *
     * @param id           Unique step id for memoization
     * @param waitEvent    The name of the event to wait for
     * @param timeout      How long to wait for the event (ms-compatible time string)
     * @param ifExpression Optional CEL expression to conditionally match the event (nullable).
     *                     Both the original trigger ({@code event}) and the incoming event
     *                     ({@code async}) are accessible via dot-notation.
     */
    public Object waitForEvent(
            String id,
            String waitEvent,
            String timeout,
            String ifExpression) {

        String hashedId = state.getHashFromId(id);

        try {
            Object stepResult = state.getState(hashedId, Object.class);
            if (stepResult != null) {
                return stepResult;
            }
            // null means the wait completed with no matching event
            return null;
        } catch (StateNotFound e) {
            throw new StepInterruptWaitForEventException(id, hashedId, waitEvent, timeout, ifExpression);
        }
    }

    /** Convenience overload — no ifExpression. */
    public Object waitForEvent(String id, String waitEvent, String timeout) {
        return waitForEvent(id, waitEvent, timeout, null);
    }

}
