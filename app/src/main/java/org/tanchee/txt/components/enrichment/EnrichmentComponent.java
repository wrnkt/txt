package org.tanchee.txt.components.enrichment;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.tanchee.txt.core.component.ComponentMetadata;
import org.tanchee.txt.core.event.EnrichmentCompletedEvent;
import org.tanchee.txt.core.event.EnrichmentRequestedEvent;
import org.tanchee.txt.core.processor.ManagedProcessor;

public class EnrichmentComponent extends ManagedProcessor<EnrichmentRequestedEvent> {

    private final Map<String, TextEnricher> enrichers;

    public EnrichmentComponent(
    Map<String, TextEnricher> enrichers
    ) {
        super(
            "enrichment-component",
            new ComponentMetadata(
                "enrichment-component",
                "0.0.1",
                Set.of("enrichment"),
                Set.of(EnrichmentRequestedEvent.class),
                Set.of(EnrichmentCompletedEvent.class)
            ),
            EnrichmentRequestedEvent.class
        );
        this.enrichers = enrichers;
    }

    @Override
    protected CompletionStage<Void> process(EnrichmentRequestedEvent event) {
        return CompletableFuture.allOf(
            event.enrichers().stream()
            .map(this::lookup)
            .filter(Objects::nonNull)
            .map(enricher -> enricher.enrich(event.text())
                .thenAccept(result -> {
                    context.eventBus().publish(
                        new EnrichmentCompletedEvent(
                            UUID.randomUUID(),
                            Instant.now(),
                            event.correlationId(),
                            1,
                            event.itemId(),
                            enricher.getClass().getSimpleName(),
                            result,
                            Map.of()
                        )
                    );
                })
            )
            .toArray(CompletableFuture[]::new)
        );
    }

    private TextEnricher lookup(String name) {
        return enrichers.get(name);
    }

    public void doStart() {}

    public void doStop() {}

}
