package org.tanchee.tealflow.enrichment;

import org.tanchee.txt.components.enrichment.TextEnricher;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Computes basic readability statistics for a chunk of text.
 *
 * <p>Returned keys:
 * <ul>
 *   <li>{@code wordCount} — number of whitespace-delimited tokens</li>
 *   <li>{@code charCount} — total character count</li>
 *   <li>{@code sentenceCount} — approximate sentence count (. ! ? terminators)</li>
 *   <li>{@code avgWordsPerSentence} — wordCount / sentenceCount</li>
 * </ul>
 */
public class WordCountEnricher implements TextEnricher {

    @Override
    public CompletableFuture<Map<String, Object>> enrich(String text) {
        return CompletableFuture.supplyAsync(() -> {
            String stripped = text.strip();
            if (stripped.isEmpty()) {
                return Map.of("wordCount", 0, "charCount", 0, "sentenceCount", 0,
                    "avgWordsPerSentence", 0.0);
            }

            int wordCount = stripped.split("\\s+").length;
            long sentenceCount = stripped.chars()
                .filter(c -> c == '.' || c == '!' || c == '?')
                .count();
            double avgWords = sentenceCount > 0 ? (double) wordCount / sentenceCount : 0.0;

            return Map.of(
                "wordCount",           wordCount,
                "charCount",           stripped.length(),
                "sentenceCount",       sentenceCount,
                "avgWordsPerSentence", Math.round(avgWords * 10.0) / 10.0
            );
        });
    }
}
