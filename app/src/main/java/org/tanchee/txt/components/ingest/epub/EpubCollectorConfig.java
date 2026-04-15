package org.tanchee.txt.components.ingest.epub;

import java.nio.file.Path;
import java.util.Set;

/**
 * Configuration for {@link EpubCollector}.
 * Register an instance against the collector's {@link org.tanchee.txt.core.component.ComponentId}
 * in a {@link org.tanchee.tealflow.context.SimpleConfigHandle} before starting the collector.
 *
 * @param epubPath              path to the .epub file
 * @param includeMetadata       whether to attach chapter/book metadata to each event
 * @param excludeChapterTitles  chapter titles to skip (cover, ToC, copyright pages, etc.)
 * @param minChapterLength      minimum character count; shorter chapters are skipped
 */
public record EpubCollectorConfig(
    Path epubPath,
    boolean includeMetadata,
    Set<String> excludeChapterTitles,
    int minChapterLength
) {
    /** Sensible defaults: include metadata, skip nothing, require at least 100 chars. */
    public static EpubCollectorConfig of(Path path) {
        return new EpubCollectorConfig(path, true, Set.of(), 100);
    }

    /** Full builder-style factory for when you need exclusions or a different threshold. */
    public static EpubCollectorConfig of(
            Path path,
            Set<String> excludeTitles,
            int minLength) {
        return new EpubCollectorConfig(path, true, Set.copyOf(excludeTitles), minLength);
    }
}
