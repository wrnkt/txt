package org.tanchee.dam.util.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum HashAlgorithm {
    MD5("MD5"),
    SHA_256("SHA-256");

    private final String name;

    HashAlgorithm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance(name);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
