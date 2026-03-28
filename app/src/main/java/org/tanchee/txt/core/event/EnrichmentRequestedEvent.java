package org.tanchee.txt.core.event;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record EnrichmentRequestedEvent(
    UUID id,
    Instant timestamp,
    UUID correlationId,
    int version,
    String itemId,
    String text,
    List<String> enrichers,
    Map<String, Object> metadata
) implements Event {}
