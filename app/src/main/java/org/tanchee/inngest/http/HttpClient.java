package org.tanchee.inngest.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public final class HttpClient {

    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    private final RequestConfig clientConfig;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public HttpClient(RequestConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.client = new OkHttpClient();

        this.objectMapper = new ObjectMapper();

        // Register custom serializers/modules here if needed:
        // SimpleModule module = new SimpleModule();
        // module.addSerializer(KlaxonDuration.class, new DurationSerializer());
        // module.addSerializer(KlaxonConcurrencyScope.class, new ConcurrencyScopeSerializer());
        // objectMapper.registerModule(module);
    }

    public <T> T send(Request request, ResponseHandler<T> handler) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            return handler.handle(response);
        }
    }

    public Request build(
            String url,
            Object payload,
            Map<String, String> queryParams,
            RequestConfig config
    ) {
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(url).newBuilder();

        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                httpUrlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request payload", e);
        }

        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, jsonRequestBody);

        Map<String, String> clientHeaders =
                clientConfig != null && clientConfig.getHeaders() != null
                        ? clientConfig.getHeaders()
                        : Collections.emptyMap();

        Map<String, String> requestHeaders =
                config != null && config.getHeaders() != null
                        ? config.getHeaders()
                        : Collections.emptyMap();

        Map<String, String> mergedHeaders = new java.util.HashMap<>(clientHeaders);
        mergedHeaders.putAll(requestHeaders);

        return new Request.Builder()
                .url(httpUrlBuilder.build())
                .post(body)
                .headers(toOkHttpHeaders(mergedHeaders))
                .build();
    }

    public static Headers toOkHttpHeaders(Map<String, String> requestHeaders) {
        Headers.Builder builder = new Headers.Builder();

        if (requestHeaders != null) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    @FunctionalInterface
    public interface ResponseHandler<T> {
        T handle(Response response) throws IOException;
    }
}
