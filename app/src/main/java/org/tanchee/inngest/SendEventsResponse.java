package org.tanchee.inngest;

import java.util.List;
import java.util.Objects;

public final class SendEventsResponse {

    private final String[] ids;

    public SendEventsResponse(String... ids) {
        this.ids = ids;
    }

    public String[] getIds() {
        return ids;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof SendEventsResponse that)
            return Objects.equals(ids, that.ids);
        return false;
    }

    @Override
    public int hashCode() {
        // NOTE: original
        // return Arrays.hashCode(ids.toArray());
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ids == null) ? 0 : ids.hashCode());
        return result;
    }
}
