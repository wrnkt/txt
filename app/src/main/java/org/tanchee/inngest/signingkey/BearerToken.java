package org.tanchee.inngest.signingkey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BearerToken {

    private static final Pattern SIGNING_KEY_REGEX =
            Pattern.compile("^(?<prefix>signkey-\\w+-)(?<key>.*)$");

    public static String hashedSigningKey(String signingKey) {
        Matcher matcher = SIGNING_KEY_REGEX.matcher(signingKey);

        if(!matcher.matches())
            throw new InvalidSigningKeyException();

        String prefix = matcher.group("prefix");
        String key = matcher.group("key");

        byte[] keyBytes;
        try {
            keyBytes = hexToByteArray(key);
        } catch (IllegalArgumentException e) {
            throw new InvalidSigningKeyException();
        }

        byte[] hashedKey;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hashedKey = digest.digest(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available ", e);
        }
        return prefix + toHexString(hashedKey);
    }

    public static final Map<String, String> getAuthorizationHeader(String signingKey) {
        String hashedSigningKey = hashedSigningKey(signingKey);
        return Map.of("Authorization", "Bearer " + hashedSigningKey);
    }

    private static byte[] hexToByteArray(String hex) {
        if ((hex.length() & 1) != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }

        byte[] bytes = new byte[hex.length() / 2];

        for (int i = 0; i < hex.length(); i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);

            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("Invalid hex character");
            }

            bytes[i / 2] = (byte) ((high << 4) + low);
        }
        return bytes;
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }
}
