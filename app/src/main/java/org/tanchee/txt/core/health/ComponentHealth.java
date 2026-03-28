package org.tanchee.txt.core.health;

import java.time.Instant;
import java.util.Map;

public record ComponentHealth(
    HealthStatus status,
    String message,
    Instant lastUpdated,
    Map<String, Object> details
) {}
