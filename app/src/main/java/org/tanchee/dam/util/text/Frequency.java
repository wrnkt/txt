package org.tanchee.dam.util.text;

import java.util.HashMap;
import java.util.Map;

public final class Frequency {

    public static enum Gram {
        UNI_GRAM(1),
        BI_GRAM(2),
        TRI_GRAM(3),
        QUA_GRAM(4);

        private final int value;

        Gram(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private Frequency() {}

    // TODO: fix, this doesn't work as intended. will possibly have multiple overlapping n-grams
    //       at the end. the {@link CharSequence} has to be divided into unique pieces.
    public static Map<CharSequence, Integer> getTermFrequencyMap(CharSequence s, Gram nGram) {
        int gramSize = nGram.getValue();
        Map<CharSequence, Integer> freqMap = new HashMap<>();

        for (int i = 0; i < s.length() - 1; i++) {
            CharSequence gram = s.subSequence(i, i + gramSize);
            freqMap.put(gram, freqMap.getOrDefault(gram, 0) + 1);
        }

        return freqMap;
    }

    public static Map<CharSequence, Integer> getTermFrequencyMap(CharSequence s) {
        return getTermFrequencyMap(s, Gram.BI_GRAM);
    }
}
