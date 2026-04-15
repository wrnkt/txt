package org.tanchee.tealflow.enrichment;

import org.tanchee.txt.components.enrichment.TextEnricher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Classifies a chunk of text against a fixed keyword taxonomy and returns
 * the matched category names as tags.
 *
 * <p>This is a simple keyword-presence approach suitable for bootstrapping.
 * Replace with an embedding-based classifier or an LLM call as the system matures.
 *
 * <p>Returned keys:
 * <ul>
 *   <li>{@code tags} — {@code List<String>} of matched category names, possibly empty</li>
 * </ul>
 *
 * <p>Categories and their trigger words are defined in {@link #TAXONOMY}. Extend or
 * replace the map to cover the domain you care about.
 */
public class KeywordTaggerEnricher implements TextEnricher {

    private static final Map<String, List<String>> TAXONOMY = Map.of(
        "health",       List.of("exercise", "diet", "sleep", "nutrition", "vitamin",
                                "fitness", "hydrate", "protein", "calorie", "posture"),
        "productivity", List.of("focus", "habit", "schedule", "goal", "routine",
                                "system", "priority", "deadline", "procrastinate", "deep work"),
        "cooking",      List.of("recipe", "ingredient", "cook", "bake", "simmer",
                                "saute", "roast", "marinate", "season", "knife"),
        "finance",      List.of("invest", "budget", "saving", "debt", "expense",
                                "income", "compound", "interest", "portfolio", "retire"),
        "home",         List.of("clean", "organize", "declutter", "repair", "paint",
                                "furniture", "storage", "laundry", "garden", "maintenance"),
        "relationships",List.of("communication", "listen", "empathy", "boundary",
                                "conflict", "trust", "appreciate", "partner", "family")
    );

    @Override
    public CompletableFuture<Map<String, Object>> enrich(String text) {
        return CompletableFuture.supplyAsync(() -> {
            String lower = text.toLowerCase();
            List<String> matched = TAXONOMY.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(lower::contains))
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());

            return Map.of("tags", matched);
        });
    }
}
