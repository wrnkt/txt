package org.tanchee.txt.core.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EnrichmentCompletedEvent(
    UUID id,
    Instant timestamp,
    UUID correlationId,
    int version,
    String itemId,
    String enricher,
    Map<String, Object> result,
    Map<String, Object> metadata
) implements Event {}
