package org.tanchee.txt.core.runtime;

public record RuntimeControl(
    boolean enabled,
    int maxConcurrency,
    int rateLimitPerSecond
) {}
