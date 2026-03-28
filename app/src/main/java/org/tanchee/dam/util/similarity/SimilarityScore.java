package org.tanchee.dam.util.similarity;

import java.util.function.BiFunction;

public interface SimilarityScore<T, R> extends BiFunction<T, T, R> {
    @Override
    R apply(T left, T right);
}
