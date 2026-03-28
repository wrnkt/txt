package org.tanchee.txt.core.component;

import java.util.Set;

import org.tanchee.txt.core.event.Event;

public record ComponentMetadata(
    String name,
    String version,
    Set<String> tags,
    Set<Class<? extends Event>> consumes,
    Set<Class<? extends Event>> produces
) {
}
