package org.tanchee.txt.core.text;

import java.util.Map;

public record ProcessedText(
    String type,
    String id,
    String raw,
    Map<String, Object> metadata
) {}
