package org.tanchee.inngest.signingkey;

public class InvalidSigningKeyException extends RuntimeException {
    public static final String MSG = "Signing key does not match expected format";
    public InvalidSigningKeyException() {
        super(MSG);
    }
}

