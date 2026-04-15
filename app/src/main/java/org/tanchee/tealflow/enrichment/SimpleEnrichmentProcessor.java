package org.tanchee.tealflow.enrichment;

import org.tanchee.txt.components.enrichment.TextEnricher;
import org.tanchee.txt.core.component.ComponentMetadata;
import org.tanchee.txt.core.component.ManagedComponent;
import org.tanchee.txt.core.event.EnrichmentCompletedEvent;
import org.tanchee.txt.core.event.EnrichmentRequestedEvent;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Subscribes to {@link EnrichmentRequestedEvent} and runs each requested enricher
 * concurrently. Publishes one {@link EnrichmentCompletedEvent} per enricher that
 * completes successfully.
 *
 * <p>Enrichers are supplied at construction time as a name-keyed map so that the
 * runner can wire any combination of {@link TextEnricher} implementations without
 * subclassing. Unknown enricher names in the event are silently skipped.
 *
 * <p>Thread safety: all enrichers execute on virtual threads via the context executor.
 * The {@code handlers} map is immutable after construction.
 */
public class SimpleEnrichmentProcessor extends ManagedComponent {

    /** Enricher name → implementation, set at construction. Immutable. */
    private final Map<String, TextEnricher> enrichers;

    public SimpleEnrichmentProcessor(String id, Map<String, TextEnricher> enrichers) {
        super(id, new ComponentMetadata(
            "enrichment-processor",
            "1.0.0",
            Set.of("enrichment"),
            Set.of(EnrichmentRequestedEvent.class),
            Set.of(EnrichmentCompletedEvent.class)
        ));
        this.enrichers = Map.copyOf(enrichers);
    }

    // ── ManagedComponent lifecycle ────────────────────────────────────────────

    @Override
    protected void doStart() {
        context.eventBus().subscribe(EnrichmentRequestedEvent.class, this::handleEnrichment);
    }

    @Override
    protected void doStop() {}

    // ── Event handler ─────────────────────────────────────────────────────────

    private CompletionStage<Void> handleEnrichment(EnrichmentRequestedEvent event) {
        stats.workerStarted();

        // Run each requested enricher concurrently; publish a result event per enricher.
        List<CompletableFuture<Void>> futures = event.enrichers().stream()
            .filter(enrichers::containsKey)
            .map(name -> runEnricher(name, enrichers.get(name), event))
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .whenComplete((v, ex) -> {
                stats.workerFinished();
                if (ex != null) stats.failed();
                else            stats.processed();
            });
    }

    private CompletableFuture<Void> runEnricher(String name,
                                                 TextEnricher enricher,
                                                 EnrichmentRequestedEvent event) {
        return enricher.enrich(event.text())
            .thenAccept(result -> context.eventBus().publish(new EnrichmentCompletedEvent(
                UUID.randomUUID(),
                Instant.now(),
                event.correlationId(),
                1,
                event.itemId(),
                name,
                result,
                event.metadata()
            )))
            .exceptionally(ex -> {
                // Log failure but don't propagate — one broken enricher shouldn't
                // cancel siblings that are already in flight.
                System.err.printf("[enrichment] %s failed for item %s: %s%n",
                    name, event.itemId(), ex.getMessage());
                stats.failed();
                return null;
            });
    }
}
