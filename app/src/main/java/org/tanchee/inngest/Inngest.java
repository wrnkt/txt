package org.tanchee.inngest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Headers;
import okhttp3.MediaType;

public final class Inngest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String appId;
    private final Map<String, String> headers;
    private final InngestEnv env;
    private final String eventKey;
    private final String baseUrl;

    private final OkHttpClient httpClient;

    public Inngest(String appId) {
        this(appId, null, null, null, null);
    }

    public Inngest(
        String appId,
        String baseUrl,
        String eventKey,
        String env,
        Boolean isDev
    ) {
        this.appId = appId;
        this.headers = Environment.inngestHeaders();
        this.env = Environment.inngestEnv(env, isDev);
        this.eventKey = Environment.inngestEventKey(eventKey);
        this.baseUrl = Environment.inngestEventApiBaseUrl(this.env, baseUrl);

        this.httpClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request originalReq = chain.request();
                Request.Builder builder = originalReq.newBuilder();
                headers.entrySet()
                    .forEach(entry -> {
                        builder.header(entry.getKey(), entry.getValue());
                    });
                return chain.proceed(builder.build());
            })
            .build();
    }

    public String appId() {
        return appId;
    }

    public InngestEnv env() {
        return env;
    }

    public Headers headers() {
        Headers.Builder builder = new Headers.Builder();
        headers.entrySet().stream()
            .forEach(e -> {
                builder.set(e.getKey(), e.getValue());
            });
        return builder.build();
    }

    public SendEventsResponse send(InngestEvent event) throws IOException {
        return send(new InngestEvent[] { event });
    }

    public SendEventsResponse send(InngestEvent... events) throws IOException {
        // TODO: handle exception. how's it originally done?
        String json = MAPPER.writeValueAsString(events);

        RequestBody reqBody = RequestBody.create(
            MediaType.parse("application/json"),
            json
        );

        Request request = new Request.Builder()
            .url("%s/e/%s".formatted(baseUrl, eventKey))
            .post(reqBody)
            .build();

        // TODO: handle exception. how's it originally done?
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody resBody = response.body();
            if (resBody == null) {
                return null;
            }

            return MAPPER.readValue(resBody.charStream(), SendEventsResponse.class);
        }
    }
}
