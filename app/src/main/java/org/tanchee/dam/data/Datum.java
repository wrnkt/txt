package org.tanchee.dam.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tanchee.dam.util.hash.HashUtils;

public class Datum {

    /**
     * The original data collected by the process retrieving it.
     */
    private final String originalContent;

    /**
     * A list of transformed versions of {@link #originalContent}
     */
    private final List<VersionedStringContent> contentVersions = new ArrayList<>();

    private String normalizedContent;

    private String contentHash;

    /**
     * Vector representation of the original content.
     * Useful for deduplication.
     */
    private List<Double> embedding;

    private String src;

    private String url;

    private List<String> tags;

    private Instant collectedTimestamp;

    private Instant associatedTimestamp;

    private boolean approved;

    private Map<String, Object> metadata;


    public Datum(String originalContent, String src, String url) {
        this.originalContent = originalContent;
        this.metadata = new HashMap<>();
        this.src = src;
        this.url = url;
        updateHash();
    }

    public void updateHash() {
        if (originalContent == null || originalContent.isBlank())
            throw new RuntimeException("Can't update hash, original content is empty");

        String hash = HashUtils.defaultHash(originalContent);
        setContentHash(hash);
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public String getNormalizedContent() {
        return normalizedContent;
    }

    public void setNormalizedContent(String normalizedContent) {
        this.normalizedContent = normalizedContent;
    }

    public String getContentHash() {
        return contentHash;
    }

    private void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    public Instant getCollectedTimestamp() {
        return collectedTimestamp;
    }

    public void setCollectedTimestamp(Instant collectedTimestamp) {
        this.collectedTimestamp = collectedTimestamp;
    }

    public Instant getAssociatedTimestamp() {
        return associatedTimestamp;
    }

    public void setAssociatedTimestamp(Instant associatedTimestamp) {
        this.associatedTimestamp = associatedTimestamp;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void addMetadataEntry(String k, Object v) {
        metadata.put(k, v);
    }

    public void addMetadataEntry(String k, String v) {
        metadata.put(k, v);
    }

    public void addVersionedContent(VersionedStringContent content) {
        contentVersions.add(content);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Datum(\n", url));
        sb.append(String.format("url=%s\n", url));
        sb.append(String.format("originalContent=\n\t%s\n", originalContent));
        sb.append(String.format(")\n", url));
        return sb.toString();
    }

}
