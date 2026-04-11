package org.tanchee.inngest;

import java.util.Arrays;

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
        if (other.getClass() != this.getClass()) return false;

        SendEventsResponse otherRes = (SendEventsResponse) other;
        return Arrays.equals(this.ids, otherRes.ids);
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
