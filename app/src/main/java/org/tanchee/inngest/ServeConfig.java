package org.tanchee.inngest;

public final class ServeConfig {

    public static final String DUMMY_SIGNING_KEY = "test";

    private final Inngest client;
    private final String id;
    private final String signingKey;
    private final String serveOrigin;
    private final String servePath;
    private final String logLevel;
    private final String baseUrl;

    public ServeConfig(Inngest client) {
        this(client, null, null, null, null, null, null);
    }

    public ServeConfig(
        Inngest client,
        String id,
        String signingKey,
        String serveOrigin,
        String servePath,
        String logLevel,
        String baseUrl
    ) {
        this.client = client;
        this.id = id;
        this.signingKey = signingKey;
        this.serveOrigin = serveOrigin;
        this.servePath = servePath;
        this.logLevel = logLevel;
        this.baseUrl = baseUrl;
    }

    public Inngest client() {
        return client;
    }

    public String appId() {
        return id != null ? id : client.appId();
    }

    public String signingKey() {
        if (signingKey != null) {
            return signingKey;
        }

        return switch (client.env()) {
            case Dev -> DUMMY_SIGNING_KEY;
            default -> {
                String envSigningKey = System.getenv(InngestSystem.SigningKey.getValue());
                if (envSigningKey == null) {
                    throw new IllegalStateException("signing key is required");
                }
                yield envSigningKey;
            }
        };
    }

    public boolean hasSigningKey() {
        return switch (signingKey()) {
            case DUMMY_SIGNING_KEY, "" -> false;
            default -> true;
        };
    }

    public String baseUrl() {
        if (baseUrl != null) {
            return baseUrl;
        }

        String envUrl = System.getenv(InngestSystem.ApiBaseUrl.getValue());
        if (envUrl != null) {
            return envUrl;
        }

        return switch (client.env()) {
            case Dev -> "http://127.0.0.1:8288";
            default -> "https://api.inngest.com";
        };
    }

    // WARN: should be OBE
    /*
    public String serveOrigin() {
        return serveOrigin != null
            ? serveOrigin
            : System.getenv(InngestSystem.ServeOrigin.getValue());
    }
    */


    public String servePath() {
        return servePath != null
            ? servePath
            : System.getenv(InngestSystem.ServePath.getValue());
    }

    public String logLevel() {
        if (logLevel != null) {
            return logLevel;
        }

        String envLogLevel = System.getenv(InngestSystem.LogLevel.getValue());
        return envLogLevel != null ? envLogLevel : "info";
    }
}
