package org.tanchee.common.function;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}
