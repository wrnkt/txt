package org.tanchee.common.optional.except;

import java.util.Optional;
import java.util.function.Consumer;

import org.tanchee.common.function.ThrowingSupplier;

public final class Try<T> {
    private final T value;
    private final Exception exception;

    private Try(T value, Exception exception) {
        this.value = value;
        this.exception = exception;
    }

    public static <T> Try<T> of(ThrowingSupplier<T> supplier) {
        try {
            return new Try<>(supplier.get(), null);
        } catch (Exception e) {
            return new Try<>(null, e);
        }
    }

    public Try<T> onSuccess(Consumer<? super T> consumer) {
        if (exception == null) {
            consumer.accept(value);
        }
        return this;
    }

    public Try<T> onException(Consumer<? super Exception> consumer) {
        if (exception != null) {
            consumer.accept(exception);
        }
        return this;
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }
}
