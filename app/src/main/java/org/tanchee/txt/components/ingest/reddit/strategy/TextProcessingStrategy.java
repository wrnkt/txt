package org.tanchee.txt.components.ingest.reddit.strategy;

import java.util.List;

import org.tanchee.txt.components.ingest.reddit.model.RedditPost;
import org.tanchee.txt.core.text.ProcessedText;

public interface TextProcessingStrategy {
    List<ProcessedText> process(RedditPost post);
}
