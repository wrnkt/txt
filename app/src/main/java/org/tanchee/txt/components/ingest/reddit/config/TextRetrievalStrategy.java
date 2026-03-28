package org.tanchee.txt.components.ingest.reddit.config;

public enum TextRetrievalStrategy {
    POST_ONLY,
    POST_AND_TOP_LEVEL_COMMENTS,
    FULL_COMMENT_TREE,
    SPLIT_LONG_TEXT,
    CHUNKED_PARAGRAPHS
}
