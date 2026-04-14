package org.tanchee.inngest;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendEventPayload {
    @JsonProperty("event_ids")
    private final String[] eventIds;

    public SendEventPayload(String... eventIds) {
        this.eventIds = eventIds;
    }

    public String[] getEventIds() {
        return eventIds;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof SendEventPayload that)) return false;
        return Arrays.equals(eventIds, that.eventIds);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(eventIds);
    }
}
