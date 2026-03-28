package org.tanchee.txt.core.config;

import java.util.function.Consumer;

import org.tanchee.txt.core.component.ComponentId;

public interface ConfigHandle {
    <T> T get(ComponentId id, Class<T> type);
    <T> void onChange(Class<T> configType, Consumer<T> listener);
}
