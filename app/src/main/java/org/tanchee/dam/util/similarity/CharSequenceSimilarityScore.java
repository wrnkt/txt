package org.tanchee.dam.util.similarity;

public interface CharSequenceSimilarityScore<R> extends SimilarityScore<CharSequence, R> {
    @Override
    R apply(CharSequence left, CharSequence right);
}
