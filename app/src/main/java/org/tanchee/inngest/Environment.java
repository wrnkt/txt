package org.tanchee.inngest;

import static java.util.Objects.nonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class Environment {

    private Environment() {}

    private static final String DUMMY_KEY_EVENT = "NO_EVENT_KEY_SET";

    public static Map<String, String> inngestHeaders() {
        return inngestHeaders(null);
    }

    public static Map<String, String> inngestHeaders(SupportedFrameworkName framework) {
        String sdk = "inngest-java:" + Version.getVersion();

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(InngestHeaderKey.ContentType.getValue(), "application/json");
        headers.put(InngestHeaderKey.Sdk.getValue(), sdk);
        headers.put(InngestHeaderKey.UserAgent.getValue(), sdk);

        Optional.ofNullable(framework)
            .map(SupportedFrameworkName::getValue)
            .ifPresentOrElse(name -> {
                headers.put(InngestHeaderKey.Framework.getValue(), name);
            }, () -> {});

        return headers;
    }

    public static String inngestEventKey() {
        return inngestEventKey(null);
    }

    public static String inngestEventKey(String key) {
        if (key != null) {
            return key;
        }

        String envKey = System.getenv(InngestSystem.EventKey.getValue());
        return nonNull(envKey) ? envKey : DUMMY_KEY_EVENT;
    }

    public static boolean isInngestEventKeySet(String value) {
        return nonNull(value) && !value.isEmpty() && !DUMMY_KEY_EVENT.equals(value);
    }

    public static String inngestEventApiBaseUrl(InngestEnv env) {
        return inngestEventApiBaseUrl(env, null);
    }

    public static String inngestEventApiBaseUrl(InngestEnv env, String url) {
        if (url != null) {
            return url;
        }

        String baseUrl = System.getenv(InngestSystem.EventApiBaseUrl.getValue());
        if (baseUrl != null) {
            return baseUrl;
        }

        return switch (env) {
            case Dev -> "http://127.0.0.1:8288";
            case Prod, Other -> "https://inn.gs";
        };
    }

    public static InngestEnv inngestEnv() {
        return inngestEnv(null, null);
    }

    public static InngestEnv inngestEnv(String env) {
        return inngestEnv(env, null);
    }

    public static InngestEnv inngestEnv(String env, Boolean isDev) {
        if (nonNull(isDev)) {
            return isDev ? InngestEnv.Dev : InngestEnv.Prod;
        }

        String sysDev = System.getenv(InngestSystem.Dev.getValue());
        if (nonNull(sysDev)) {
            return switch (sysDev) {
                case "0" -> InngestEnv.Prod;
                case "1" -> InngestEnv.Dev;
                default -> {
                    InngestEnv other = InngestEnv.Other;
                    other.setValue(sysDev);
                    yield other;
                }
            };
        }

        if (nonNull(env)) {
            return switch (env) {
                case "dev", "development" -> InngestEnv.Dev;
                case "prod", "production" -> InngestEnv.Prod;
                default -> {
                    InngestEnv other = InngestEnv.Other;
                    other.setValue(env);
                    yield other;
                }
            };
        }

        String inngestEnv = System.getenv(InngestSystem.Env.getValue());

        return switch (inngestEnv) {
            case null -> InngestEnv.Dev;
            case "dev", "development" -> InngestEnv.Dev;
            case "prod", "production" -> InngestEnv.Prod;
            default -> {
                InngestEnv other = InngestEnv.Other;
                other.setValue(inngestEnv);
                yield other;
            }
        };
    }
}
