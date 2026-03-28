package org.tanchee.txt.core.event;

import java.time.Instant;
import java.util.UUID;

public interface Event {
    UUID id();
    Instant timestamp();
    UUID correlationId();
    int version();
}
