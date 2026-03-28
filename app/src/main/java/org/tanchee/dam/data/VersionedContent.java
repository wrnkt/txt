package org.tanchee.dam.data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 *  A wrapper class to hold a piece of content with type T.
 */
public class VersionedContent<T> {

    private final T content;

    private final String version;

    public static enum Type {
        UNKNOWN,
        SHORTENED,
        EDITED
    }

    private final Type type;

    private final Map<String, Object> metadata;

    private boolean approved = false;

    private final Instant createdAt;

    public VersionedContent(T content) {
        this(content, null, new HashMap<>(), Instant.now(), false);
    }

    private VersionedContent(
        T content,
        String version,
        Map<String, Object> metadata,
        Instant createdAt,
        boolean approved
    ) {
        this.type = null;
        this.version = version;
        this.content = content;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.approved = approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isApproved() {
        return approved;
    }
    
}
