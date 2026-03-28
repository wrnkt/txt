package org.tanchee.txt.components.ingest.reddit.config;

import java.time.Duration;
import java.util.List;

public record RedditIngestConfig(
    boolean enabled,
    List<SubredditConfig> subreddits,
    int workerThreads,
    int maxConcurrentRequests,
    Duration baseBackoff,
    Duration maxBackoff,
    int maxRetries,
    TextRetrievalStrategy textRetrievalStrategy,
    List<String> downstreamEnrichers
) {
}
