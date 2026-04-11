package org.tanchee.inngest.func;

import java.util.List;

import org.tanchee.inngest.Event;

public class FunctionContext {
    private Event event;
    private List<Event> events;
    private String runId;
    private String fnId;
    private int attempt;

    public FunctionContext(Event event, List<Event> events, String runId, String fnId, int attempt) {
        this.event = event;
        this.events = events;
        this.runId = runId;
        this.fnId = fnId;
        this.attempt = attempt;
    }

    public Event getEvent() {
        return event;
    }

    public List<Event> getEvents() {
        return events;
    }

    public String getRunId() {
        return runId;
    }

    public String getFnId() {
        return fnId;
    }

    public int getAttempt() {
        return attempt;
    }

}
