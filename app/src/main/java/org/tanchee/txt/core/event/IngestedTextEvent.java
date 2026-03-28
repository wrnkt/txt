package org.tanchee.txt.core.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record IngestedTextEvent(
    UUID id,
    Instant timestamp,
    UUID correlationId,
    int version,
    String sourceType,
    String sourceId,
    String text,
    Map<String, Object> metadata
) implements Event {}
