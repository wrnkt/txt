package org.tanchee.txt.components.ingest.epub;

import org.jsoup.Jsoup;
import org.tanchee.txt.core.component.ComponentMetadata;
import org.tanchee.txt.core.component.ManagedComponent;
import org.tanchee.txt.core.event.IngestedTextEvent;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Ingests an ePub file by parsing it in spine order and publishing one
 * {@link IngestedTextEvent} per chapter onto the event bus.
 *
 * <p>The ePub format is a ZIP archive containing:
 * <ul>
 *   <li>{@code META-INF/container.xml} — points to the OPF package document</li>
 *   <li>An OPF file — declares the manifest (item id → href) and the spine (reading order)</li>
 *   <li>HTML/XHTML content files — one per chapter or section</li>
 * </ul>
 *
 * <p>Parsing uses only {@code java.util.zip} (JDK) and jsoup (already a txt dependency);
 * no additional ePub library is required.
 *
 * <p>Lifecycle: this component is <em>run-once</em>. {@code doStart()} performs the full
 * ingest synchronously, then returns. The component transitions to {@code RUNNING} while
 * events are being published and stays there; call {@code stop()} to release it.
 *
 * <p>Requires an {@link EpubCollectorConfig} registered for this component's id in the
 * context's {@link org.tanchee.txt.core.config.ConfigHandle}.
 */
public class EpubCollector extends ManagedComponent {

    private static final String SOURCE_TYPE = "EPUB";

    public EpubCollector(String id) {
        super(id, new ComponentMetadata(
            "epub-collector",
            "1.0.0",
            Set.of("ingest", "epub"),
            Set.of(),
            Set.of(IngestedTextEvent.class)
        ));
    }

    // ── ManagedComponent lifecycle ────────────────────────────────────────────

    @Override
    protected void doStart() throws Exception {
        EpubCollectorConfig config = context.config().get(id(), EpubCollectorConfig.class);
        List<EpubChapter> chapters = parseEub(config);

        UUID correlationId = UUID.randomUUID();
        int published = 0;

        for (EpubChapter chapter : chapters) {
            if (chapter.text().length() < config.minChapterLength()) continue;
            if (config.excludeChapterTitles().contains(chapter.title())) continue;

            context.eventBus().publish(buildEvent(chapter, config, correlationId));
            stats.processed();
            published++;
        }

        context.state().put(id(), "chaptersPublished", published);
        context.state().put(id(), "lastRunAt", Instant.now().toString());
    }

    @Override
    protected void doStop() {
        // stateless — nothing to release
    }

    // ── Event construction ────────────────────────────────────────────────────

    private IngestedTextEvent buildEvent(EpubChapter chapter,
                                         EpubCollectorConfig config,
                                         UUID correlationId) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (config.includeMetadata()) {
            metadata.put("bookTitle",     chapter.bookTitle());
            metadata.put("chapterTitle",  chapter.title());
            metadata.put("chapterIndex",  chapter.index());
            metadata.put("epubPath",      config.epubPath().toString());
        }
        return new IngestedTextEvent(
            UUID.randomUUID(),
            Instant.now(),
            correlationId,
            1,
            SOURCE_TYPE,
            config.epubPath().getFileName() + "#" + chapter.index(),
            chapter.text(),
            Collections.unmodifiableMap(metadata)
        );
    }

    // ── ePub parsing ──────────────────────────────────────────────────────────

    private List<EpubChapter> parseEpub(EpubCollectorConfig config) throws Exception {
        try (ZipFile zip = new ZipFile(config.epubPath().toFile())) {
            String opfPath      = findOpfPath(zip);
            EpubManifest manifest = parseOpf(zip, opfPath);

            // OPF-relative hrefs must be resolved against the OPF directory.
            String opfDir = opfPath.contains("/")
                ? opfPath.substring(0, opfPath.lastIndexOf('/') + 1)
                : "";

            List<EpubChapter> chapters = new ArrayList<>();
            for (int i = 0; i < manifest.spineHrefs().size(); i++) {
                String entryPath = opfDir + manifest.spineHrefs().get(i);
                ZipEntry entry   = zip.getEntry(entryPath);
                if (entry == null) continue;

                try (InputStream is = zip.getInputStream(entry)) {
                    org.jsoup.nodes.Document html = Jsoup.parse(is, "UTF-8", "");

                    String title = html.title().isBlank()
                        ? "Chapter " + (i + 1)
                        : html.title().strip();

                    String text = html.body() != null
                        ? html.body().text()
                        : "";

                    chapters.add(new EpubChapter(i, title, manifest.bookTitle(), text));
                }
            }
            return chapters;
        }
    }

    /**
     * Reads {@code META-INF/container.xml} to locate the OPF package document.
     * The {@code full-path} attribute of the first {@code <rootfile>} element is returned.
     */
    private String findOpfPath(ZipFile zip) throws Exception {
        ZipEntry containerEntry = zip.getEntry("META-INF/container.xml");
        if (containerEntry == null) {
            throw new IllegalArgumentException(
                "Not a valid ePub: missing META-INF/container.xml in " + zip.getName());
        }
        try (InputStream is = zip.getInputStream(containerEntry)) {
            Document doc = DocumentBuilderFactory.newDefaultInstance()
                .newDocumentBuilder()
                .parse(is);
            NodeList rootfiles = doc.getElementsByTagName("rootfile");
            if (rootfiles.getLength() == 0) {
                throw new IllegalArgumentException("No <rootfile> found in container.xml");
            }
            return rootfiles.item(0)
                .getAttributes()
                .getNamedItem("full-path")
                .getTextContent();
        }
    }

    /**
     * Parses the OPF document to extract:
     * <ul>
     *   <li>the book title from {@code dc:title}</li>
     *   <li>the spine-ordered list of HTML content hrefs</li>
     * </ul>
     */
    private EpubManifest parseOpf(ZipFile zip, String opfPath) throws Exception {
        ZipEntry opfEntry = zip.getEntry(opfPath);
        if (opfEntry == null) {
            throw new IllegalArgumentException("OPF file not found at path: " + opfPath);
        }
        try (InputStream is = zip.getInputStream(opfEntry)) {
            Document doc = DocumentBuilderFactory.newDefaultInstance()
                .newDocumentBuilder()
                .parse(is);

            // Book title
            NodeList titleNodes = doc.getElementsByTagName("dc:title");
            String bookTitle = titleNodes.getLength() > 0
                ? titleNodes.item(0).getTextContent().strip()
                : "Unknown";

            // id → href map, filtered to HTML/XHTML items only
            Map<String, String> idToHref = new LinkedHashMap<>();
            NodeList items = doc.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                var attrs     = items.item(i).getAttributes();
                String itemId = attrs.getNamedItem("id").getTextContent();
                String href   = attrs.getNamedItem("href").getTextContent();
                var mediaTypeAttr = attrs.getNamedItem("media-type");
                String mediaType  = mediaTypeAttr != null ? mediaTypeAttr.getTextContent() : "";
                if (mediaType.contains("html") || mediaType.contains("xhtml")) {
                    idToHref.put(itemId, href);
                }
            }

            // Spine: ordered list of idrefs → resolved hrefs
            List<String> spineHrefs = new ArrayList<>();
            NodeList spineItems = doc.getElementsByTagName("itemref");
            for (int i = 0; i < spineItems.getLength(); i++) {
                String idref = spineItems.item(i)
                    .getAttributes()
                    .getNamedItem("idref")
                    .getTextContent();
                String href = idToHref.get(idref);
                if (href != null) spineHrefs.add(href);
            }

            return new EpubManifest(bookTitle, spineHrefs);
        }
    }

    /** Internal value type: the parts of the OPF document we care about. */
    private record EpubManifest(String bookTitle, List<String> spineHrefs) {}
}
