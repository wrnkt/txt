package org.tanchee.txt.core.monitoring;

import java.time.Instant;
import java.util.Map;

public record ComponentHealth(
    HealthStatus status,
    String message,
    Instant lastUpdated,
    Map<String, Object> details
) {}
