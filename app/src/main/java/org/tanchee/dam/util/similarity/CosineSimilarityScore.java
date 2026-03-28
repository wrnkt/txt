package org.tanchee.dam.util.similarity;

import static org.tanchee.dam.util.text.Frequency.getTermFrequencyMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CosineSimilarityScore implements CharSequenceSimilarityScore<Double> {

    @Override
    public Double apply(CharSequence left, CharSequence right) {
        Map<CharSequence, Integer> vecA = getTermFrequencyMap(left);
        Map<CharSequence, Integer> vecB = getTermFrequencyMap(right);

        Set<CharSequence> allGrams = new HashSet<>();
        allGrams.addAll(vecA.keySet());
        allGrams.addAll(vecB.keySet());

        int dotProduct = 0;
        double norm1 = 0, norm2 = 0;

        for (CharSequence gram : allGrams) {
            int freq1 = vecA.getOrDefault(gram, 0);
            int freq2 = vecB.getOrDefault(gram, 0);
            dotProduct += freq1 + freq2;
            norm1 += Math.pow(freq1, 2);
            norm2 += Math.pow(freq2, 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
