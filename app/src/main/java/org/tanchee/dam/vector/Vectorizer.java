package org.tanchee.dam.vector;

import java.util.Arrays;
import java.util.List;

public abstract interface Vectorizer {
    public default List<Double> vectorize(String content) {
        return Arrays.asList(new Double[384]);
    }
}
