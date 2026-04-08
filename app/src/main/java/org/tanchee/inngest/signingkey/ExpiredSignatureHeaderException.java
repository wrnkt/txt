package org.tanchee.inngest.signingkey;

public class ExpiredSignatureHeaderException extends RuntimeException {
    public ExpiredSignatureHeaderException() {
        super("signature header has expired");
    }
}
