package org.tanchee.tealflow.runner;

import org.tanchee.tealflow.context.SimpleComponentContext;
import org.tanchee.tealflow.context.SimpleConfigHandle;
import org.tanchee.tealflow.enrichment.KeywordTaggerEnricher;
import org.tanchee.tealflow.enrichment.SimpleEnrichmentProcessor;
import org.tanchee.tealflow.enrichment.WordCountEnricher;
import org.tanchee.txt.components.ingest.epub.EpubCollector;
import org.tanchee.txt.components.ingest.epub.EpubCollectorConfig;
import org.tanchee.txt.components.processing.ChunkingProcessor;
import org.tanchee.txt.core.component.ComponentId;
import org.tanchee.txt.core.event.EnrichmentCompletedEvent;
import org.tanchee.txt.core.monitoring.ComponentStats;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Local runner that wires up and executes a full ePub ingest → chunk → enrich pipeline
 * using entirely in-memory infrastructure.
 *
 * <p>Pipeline:
 * <pre>
 *   EpubCollector
 *       ↓ IngestedTextEvent (one per chapter)
 *   ChunkingProcessor
 *       ↓ EnrichmentRequestedEvent (one per chunk)
 *   SimpleEnrichmentProcessor
 *       │ WordCountEnricher  → EnrichmentCompletedEvent { enricher="word-count" }
 *       └ KeywordTaggerEnricher → EnrichmentCompletedEvent { enricher="keyword-tagger" }
 * </pre>
 *
 * <p>Usage:
 * <pre>{@code
 *   java -cp ... org.tanchee.tealflow.runner.IngestRunner path/to/book.epub
 * }</pre>
 *
 * <p>The runner starts all components, waits for the in-flight virtual threads to drain,
 * prints a summary, then shuts down cleanly.
 */
public class IngestRunner {

    // ── Component IDs ─────────────────────────────────────────────────────────

    private static final ComponentId COLLECTOR_ID   = new ComponentId("epub-collector");
    private static final ComponentId CHUNKER_ID     = new ComponentId("chunking-processor");
    private static final ComponentId ENRICHMENT_ID  = new ComponentId("enrichment-processor");

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        Path epubPath = args.length > 0
            ? Path.of(args[0])
            : Path.of("sample.epub");

        System.out.println("=== TEALFLOW ePub Ingest Pipeline ===");
        System.out.println("Source: " + epubPath.toAbsolutePath());
        System.out.println();

        // ── 1. Config ─────────────────────────────────────────────────────────
        // Excluded titles cover typical ePub front matter; adjust per book.
        EpubCollectorConfig epubConfig = EpubCollectorConfig.of(
            epubPath,
            Set.of("Cover", "Table of Contents", "Copyright", "Dedication", "Index"),
            200   // skip chapters shorter than 200 chars
        );

        SimpleConfigHandle configHandle = new SimpleConfigHandle()
            .register(COLLECTOR_ID,  epubConfig)
            .register(CHUNKER_ID,    ChunkingProcessor.ChunkingConfig.defaults());

        // ── 2. Context (all in-memory) ────────────────────────────────────────
        SimpleComponentContext ctx = new SimpleComponentContext(configHandle);

        // ── 3. Observability: count and log completed enrichments ─────────────
        AtomicInteger completedEnrichments = new AtomicInteger();
        ctx.eventBus().subscribe(EnrichmentCompletedEvent.class, event -> {
            int n = completedEnrichments.incrementAndGet();
            System.out.printf("  [%4d] %-20s  chunk=%-40s  result=%s%n",
                n,
                event.enricher(),
                truncate(event.itemId(), 40),
                event.result());
            return CompletableFuture.completedFuture(null);
        });

        // ── 4. Assemble components ────────────────────────────────────────────
        SimpleEnrichmentProcessor enrichmentProcessor = new SimpleEnrichmentProcessor(
            ENRICHMENT_ID.value(),
            Map.of(
                "word-count",     new WordCountEnricher(),
                "keyword-tagger", new KeywordTaggerEnricher()
            )
        );

        ChunkingProcessor chunkingProcessor = new ChunkingProcessor(CHUNKER_ID.value());
        EpubCollector     collector         = new EpubCollector(COLLECTOR_ID.value());

        // ── 5. Start in dependency order ──────────────────────────────────────
        // Enrichment and chunking processors must subscribe to the bus before
        // the collector starts publishing events.
        System.out.println("Starting enrichment processor...");
        enrichmentProcessor.start(ctx).toCompletableFuture().join();

        System.out.println("Starting chunking processor...");
        chunkingProcessor.start(ctx).toCompletableFuture().join();

        System.out.println("Starting ePub collector...\n");
        collector.start(ctx).toCompletableFuture().join();

        // ── 6. Drain in-flight virtual threads ────────────────────────────────
        // The collector's doStart() is synchronous (publishes then returns), but
        // the downstream handlers run asynchronously on virtual threads. We sleep
        // briefly to let them drain. In production, use a CountDownLatch or a
        // reactive completion signal instead.
        Thread.sleep(5_000);

        // ── 7. Stats ──────────────────────────────────────────────────────────
        System.out.println("\n=== Pipeline Stats ===");
        printStats("Collector  (chapters published)", ctx.stats().get(COLLECTOR_ID));
        printStats("Chunker    (chunks produced)    ", ctx.stats().get(CHUNKER_ID));
        printStats("Enrichment (enrichments done)   ", ctx.stats().get(ENRICHMENT_ID));
        System.out.printf("Total EnrichmentCompletedEvents observed: %d%n",
            completedEnrichments.get());

        // ── 8. Shutdown ───────────────────────────────────────────────────────
        enrichmentProcessor.stop().toCompletableFuture().join();
        chunkingProcessor.stop().toCompletableFuture().join();

        System.out.println("\nDone.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void printStats(String label, ComponentStats stats) {
        System.out.printf("  %-38s processed=%-6d failed=%-4d active=%d%n",
            label,
            stats.processedCount(),
            stats.failedCount(),   // NOTE: bug in txt — returns activeWorkers; track upstream fix
            stats.activeWorkers());
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
