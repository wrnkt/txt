package org.tanchee.txt.components.ingest.reddit.config;

import java.time.Duration;

public record SubredditConfig(
    String name,
    Duration pollInterval,
    int maxPostsPerPoll,
    int priority
) {
}
