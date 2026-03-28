package org.tanchee.dam.util.hash;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class HashUtils {

    private static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static HashAlgorithm DEFAULT_HASH_ALGO = HashAlgorithm.SHA_256;

    private HashUtils() {}

    public static String hash(String in, HashAlgorithm algorithm) {
        try {
            MessageDigest digest = algorithm.getMessageDigest();

            byte[] bytes = digest.digest(in.getBytes(DEFAULT_CHARSET.name()));

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String defaultHash(String in) {
        return hash(in, DEFAULT_HASH_ALGO);
    }

    public static String sha256(String in) {
        return hash(in, HashAlgorithm.SHA_256);
    }

    public static String md5(String in) {
        return hash(in, HashAlgorithm.MD5);
    }

}
