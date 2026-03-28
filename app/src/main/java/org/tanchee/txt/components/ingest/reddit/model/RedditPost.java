package org.tanchee.txt.components.ingest.reddit.model;

import java.time.Instant;

public record RedditPost(
    String title,
    String fullText,
    String author,
    Instant processTime
) {}
