package org.tanchee.inngest.signingkey;

import org.tanchee.inngest.InngestEnv;
import org.tanchee.inngest.ServeConfig;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;

public final class SignatureVerification {
    private SignatureVerification() {}

    public static final String HMAC_SHA256 = "HmacSHA256";
    public static final long FIVE_MINUTES_IN_S = 5L * 60L;

    private static final Pattern SIGNING_KEY_REGEX =
            Pattern.compile("^(?<prefix>signkey-[^-]+-)(?<key>.+)$");

    private static String computeHmac(String data, String key) {
        try {
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);

            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(secretKeySpec);

            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHex(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    public static String signRequest(
            String requestBody,
            long timestamp,
            String signingKey
    ) {
        return signRequest(requestBody, Long.toString(timestamp), signingKey);
    }

    private static String signRequest(
            String requestBody,
            String timestamp,
            String signingKey
    ) {
        Matcher matcher = SIGNING_KEY_REGEX.matcher(signingKey);
        if (!matcher.matches()) {
            throw new InvalidSigningKeyException();
        }

        String key = Objects.requireNonNull(matcher.group("key"));
        String message = requestBody + timestamp;

        return computeHmac(message, key);
    }

    public static void validateSignature(
            String signatureHeader,
            String signingKey,
            String requestBody
    ) {
        String dummyUrl = "https://test.inngest.com/?" + signatureHeader;

        HttpUrl url = HttpUrl.parse(dummyUrl);
        if (url == null) {
            throw new InvalidSignatureHeaderException(
                    "signature header does not match expected format"
            );
        }

        String timestampParam = url.queryParameter("t");
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampParam);
        } catch (Exception e) {
            throw new InvalidSignatureHeaderException("timestamp is invalid");
        }

        String signature = url.queryParameter("s");
        if (signature == null) {
            throw new InvalidSignatureHeaderException("signature is invalid");
        }

        long fiveMinutesAgo = Instant.now()
                .minusSeconds(FIVE_MINUTES_IN_S)
                .getEpochSecond();

        if (timestamp < fiveMinutesAgo) {
            throw new ExpiredSignatureHeaderException();
        }

        String actualSignature = signRequest(requestBody, timestamp, signingKey);

        if (!actualSignature.equals(signature)) {
            throw new InvalidSignatureHeaderException("signature is invalid");
        }
    }

    /**
     * Checks whether the signature header is valid for a given request.
     *
     * @param signatureHeader The X-Inngest-Signature header in the format:
     *                        "t=<seconds_since_unix_epoch>&s=<signature>"
     * @param requestBody     The request body
     * @param serverKind      The X-Inngest-Server-Kind header, either "dev" or "cloud"
     * @param config          Current ServeConfig instance
     */
    public static void checkHeadersAndValidateSignature(
            String signatureHeader,
            String requestBody,
            String serverKind,
            ServeConfig config
    ) {
        boolean useDevServer = config.client().env() == InngestEnv.Dev;

        // Exit early without checking signature if we are using dev server
        if (useDevServer) {
            if (!"dev".equals(serverKind)) {
                System.out.println(
                        "WARNING: using dev server but received X-Inngest-Server-Kind: " + serverKind
                );
            }
            return;
        }

        String signingKey = config.signingKey();

        if (signatureHeader == null) {
            throw new InvalidSignatureHeaderException(
                    "Using cloud inngest but did not receive X-Inngest-Signature"
            );
        }

        validateSignature(signatureHeader, signingKey, requestBody);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
