package org.tanchee.inngest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendEventPayload {
    @JsonProperty("event_ids")
    private final List<String> eventIds;

    public SendEventPayload(List<String> eventIds) {
        this.eventIds = eventIds;
    }

    public List<String> getEventIds() {
        return eventIds;
    }
}
