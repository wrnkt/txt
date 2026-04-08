package org.tanchee.inngest.signingkey;

public class InvalidSignatureHeaderException extends RuntimeException {
    public InvalidSignatureHeaderException(String message) {
        super(message);
    }
}
