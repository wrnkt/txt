package org.tanchee.common;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Functional {

    public static <K, V> Map<K, V> mapOf(Consumer<Map<K, V>> consumer) {
        return mapOf(HashMap::new, consumer);
    }

    public static <K, V> Map<K, V> mapOf(
        Supplier<Map<K, V>> producer,
        Consumer<Map<K, V>> consumer
    ) {
        Map<K, V> map = new HashMap<>();
        consumer.accept(map);
        return map;
    }
}
