package org.tanchee.txt.components.processing;

import org.tanchee.txt.core.component.ComponentMetadata;
import org.tanchee.txt.core.component.ManagedComponent;
import org.tanchee.txt.core.event.EnrichmentRequestedEvent;
import org.tanchee.txt.core.event.IngestedTextEvent;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Subscribes to {@link IngestedTextEvent}, splits the raw text into smaller chunks,
 * and publishes one {@link EnrichmentRequestedEvent} per chunk.
 *
 * <p>Two chunking strategies are supported:
 * <ul>
 *   <li>{@code PARAGRAPH} — accumulate paragraphs (double-newline separated) up to
 *       {@code maxChunkSize} characters, then start a new chunk. Good for prose.</li>
 *   <li>{@code FIXED_SIZE} — slide a window of {@code maxChunkSize} characters with
 *       {@code overlap} characters of context carry-over. Good for dense/structured text.</li>
 * </ul>
 *
 * <p>Configuration: register a {@link ChunkingConfig} for this component's id before starting.
 */
public class ChunkingProcessor extends ManagedComponent {

    public ChunkingProcessor(String id) {
        super(id, new ComponentMetadata(
            "chunking-processor",
            "1.0.0",
            Set.of("processing"),
            Set.of(IngestedTextEvent.class),
            Set.of(EnrichmentRequestedEvent.class)
        ));
    }

    // ── ManagedComponent lifecycle ────────────────────────────────────────────

    @Override
    protected void doStart() {
        ChunkingConfig config = context.config().get(id(), ChunkingConfig.class);
        context.eventBus().subscribe(IngestedTextEvent.class,
            event -> handleIngest(event, config));
    }

    @Override
    protected void doStop() {}

    // ── Event handler ─────────────────────────────────────────────────────────

    private CompletionStage<Void> handleIngest(IngestedTextEvent event, ChunkingConfig config) {
        return CompletableFuture.runAsync(() -> {
            stats.workerStarted();
            try {
                List<String> chunks = chunk(event.text(), config);

                for (int i = 0; i < chunks.size(); i++) {
                    Map<String, Object> meta = new LinkedHashMap<>(event.metadata());
                    meta.put("chunkIndex",  i);
                    meta.put("chunkTotal",  chunks.size());
                    meta.put("sourceType",  event.sourceType());
                    meta.put("sourceId",    event.sourceId());

                    context.eventBus().publish(new EnrichmentRequestedEvent(
                        UUID.randomUUID(),
                        Instant.now(),
                        event.correlationId(),
                        1,
                        event.sourceId() + "#chunk-" + i,
                        chunks.get(i),
                        List.of("word-count", "keyword-tagger"),
                        Collections.unmodifiableMap(meta)
                    ));
                }
                stats.processed();
            } catch (Exception ex) {
                stats.failed();
                throw ex;
            } finally {
                stats.workerFinished();
            }
        }, context.executor());
    }

    // ── Chunking strategies ───────────────────────────────────────────────────

    private List<String> chunk(String text, ChunkingConfig config) {
        return switch (config.strategy()) {
            case PARAGRAPH  -> chunkByParagraph(text, config.maxChunkSize());
            case FIXED_SIZE -> chunkByFixedSize(text, config.maxChunkSize(), config.overlap());
        };
    }

    /**
     * Accumulates double-newline-separated paragraphs into chunks.
     * When the next paragraph would push the current chunk over {@code maxSize},
     * the current chunk is finalized and a new one started.
     */
    private List<String> chunkByParagraph(String text, int maxSize) {
        String[] paragraphs = text.split("\\n{2,}");
        List<String> chunks  = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String para : paragraphs) {
            String p = para.strip();
            if (p.isEmpty()) continue;

            // A single paragraph exceeding maxSize gets its own chunk.
            if (!current.isEmpty() && current.length() + 2 + p.length() > maxSize) {
                chunks.add(current.toString().strip());
                current.setLength(0);
            }
            if (!current.isEmpty()) current.append("\n\n");
            current.append(p);
        }
        if (!current.isEmpty()) chunks.add(current.toString().strip());
        return chunks;
    }

    /**
     * Slides a fixed-size window over the text.
     * {@code overlap} characters from the end of each chunk are repeated at the
     * start of the next to preserve context across boundaries.
     */
    private List<String> chunkByFixedSize(String text, int size, int overlap) {
        if (overlap >= size) throw new IllegalArgumentException("overlap must be < size");
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + size, text.length());
            chunks.add(text.substring(start, end));
            start += size - overlap;
        }
        return chunks;
    }

    // ── Config ────────────────────────────────────────────────────────────────

    /**
     * Immutable configuration for {@link ChunkingProcessor}.
     *
     * @param strategy     how to split the text
     * @param maxChunkSize maximum characters per chunk
     * @param overlap      characters to repeat between adjacent chunks (FIXED_SIZE only)
     */
    public record ChunkingConfig(Strategy strategy, int maxChunkSize, int overlap) {

        public enum Strategy { PARAGRAPH, FIXED_SIZE }

        /** Paragraph-based, 2 000-char max, no overlap. */
        public static ChunkingConfig defaults() {
            return new ChunkingConfig(Strategy.PARAGRAPH, 2_000, 0);
        }

        public static ChunkingConfig fixedSize(int size, int overlap) {
            return new ChunkingConfig(Strategy.FIXED_SIZE, size, overlap);
        }
    }
}
