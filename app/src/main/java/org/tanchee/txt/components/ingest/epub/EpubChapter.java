package org.tanchee.txt.components.ingest.epub;

/**
 * Represents a single chapter extracted from an ePub file, in spine order.
 *
 * @param index     zero-based position in the spine
 * @param title     chapter title (from HTML {@code <title>} or a generated fallback)
 * @param bookTitle book-level title from the OPF {@code dc:title} element
 * @param text      plain text extracted from the chapter HTML (via jsoup)
 */
public record EpubChapter(
    int index,
    String title,
    String bookTitle,
    String text
) {}
