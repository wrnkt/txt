package org.tanchee.dam.util.similarity;

public final class LevenshteinDistance implements CharSequenceDistance<Integer> {

    private LevenshteinDistance() {}

    @Override
    public Integer apply(CharSequence left, CharSequence right) {
        int lenLeft = left.length();
        int lenRight = right.length();

        int[][] dp = new int[lenLeft + 1][lenRight + 1];

        for (int i = 0; i <= lenLeft; i++) {
            dp[i][0] = i;
        }
        
        for (int i = 0; i <= lenLeft; i++) {
            dp[0][i] = i;
        }

        for (int i = 1; i <= lenLeft; i++) {
            for (int j = 1; j <= lenRight; j++) {
                int cost = (left.charAt(i - 1) == right.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = 
                    Math.min(
                        Math.min(
                            dp[i - 1][j] + 1,     // deletion
                            dp[i][j - 1] + 1      // insertion
                        ),
                        dp[i - 1][j - 1] + cost   // substitution
                    );
            }
        }

        return dp[lenLeft][lenRight];
    }

}
