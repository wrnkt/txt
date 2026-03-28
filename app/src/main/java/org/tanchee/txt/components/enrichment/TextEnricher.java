package org.tanchee.txt.components.enrichment;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface TextEnricher {
    CompletableFuture<Map<String, Object>> enrich(String text);
}
