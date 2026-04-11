package org.tanchee.inngest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.tanchee.inngest.signingkey.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

import static org.tanchee.inngest.signingkey.BearerToken.getAuthorizationHeader;
import static org.tanchee.inngest.signingkey.BearerToken.hashedSigningKey;

public class CommHandler {

    public static class ExecutionRequestPayload {
        public ExecutionContext ctx;
        public Event event;
        public List<Event> events;
        public MemoizedState steps;

        public ExecutionRequestPayload() {
        }
    }

    public static class ExecutionContext {
        public int attempt;

        @JsonProperty("fn_id")
        public String fnId;

        @JsonProperty("run_id")
        public String runId;

        public String env;

        public ExecutionContext() {
        }
    }

    public static class RegistrationRequestPayload {

        public String appName;
        public String deployType = "ping";
        public String framework;
        public List<InternalFunctionConfig> functions = new ArrayList<>();
        public String sdk;
        public String url;
        public String v;

        public RegistrationRequestPayload() {
        }

        public RegistrationRequestPayload(
                String appName,
                String framework,
                List<InternalFunctionConfig> functions,
                String sdk,
                String url,
                String v
        ) {
            this.appName = appName;
            this.framework = framework;
            this.functions = functions;
            this.sdk = sdk;
            this.url = url;
            this.v = v;
        }
    }

    public static class CommResponse {
        public final String body;
        public final ResultStatusCode statusCode;
        public final Map<String, String> headers;

        public CommResponse(
                String body,
                ResultStatusCode statusCode,
                Map<String, String> headers
        ) {
            this.body = body;
            this.statusCode = statusCode;
            this.headers = headers;
        }
    }

    public static class CommError {
        public final String name;
        public final String message;
        public final String stack;

        @JsonProperty("__serialized")
        public final boolean serialized = true;

        public CommError(String name, String message, String stack) {
            this.name = name;
            this.message = message;
            this.stack = stack;
        }
    }

    private static final Set<ResultStatusCode> STEP_TERMINAL_STATUS_CODES = Set.of(
            ResultStatusCode.StepComplete,
            ResultStatusCode.StepError
    );

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private final Inngest client;
    private final ServeConfig config;
    private final SupportedFrameworkName framework;
    private final Map<String, String> headers;
    private final Map<String, InternalInngestFunction> functions;

    public CommHandler(
            Map<String, InngestFunction> functions,
            Inngest client,
            ServeConfig config,
            SupportedFrameworkName framework
    ) {
        this.client = client;
        this.config = config;
        this.framework = framework;

        Map<String, String> mergedHeaders = new HashMap<>();
        mergedHeaders.putAll(Environment.inngestHeaders(framework));
        mergedHeaders.putAll(client.getHeaders());
        this.headers = Collections.unmodifiableMap(mergedHeaders);

        Map<String, InternalInngestFunction> internalFunctions = new HashMap<>();

        for (Map.Entry<String, InngestFunction> entry : functions.entrySet()) {
            internalFunctions.put(
                    entry.getKey(),
                    entry.getValue().toInngestFunction()
            );
        }

        internalFunctions.putAll(generateFailureFunctions(functions, client));

        this.functions = Collections.unmodifiableMap(internalFunctions);
    }

    private static Map<String, InternalInngestFunction> generateFailureFunctions(
            Map<String, InngestFunction> functions,
            Inngest client
    ) {
        Map<String, InternalInngestFunction> result = new HashMap<>();

        for (InngestFunction fn : functions.values()) {
            InternalInngestFunction failureHandler = fn.toFailureHandler(client.getAppId());
            if (failureHandler != null && failureHandler.id() != null) {
                result.put(failureHandler.id(), failureHandler);
            }
        }

        return result;
    }

    public CommResponse callFunction(
            String functionId,
            String requestBody
    ) {
        try {
            ExecutionRequestPayload payload =
                    OBJECT_MAPPER.readValue(requestBody, ExecutionRequestPayload.class);

            InternalInngestFunction function = functions.get(functionId);
            if (function == null) {
                throw new IllegalStateException("Function not found");
            }

            FunctionContext ctx = new FunctionContext(
                    payload.event,
                    payload.events,
                    payload.ctx.runId,
                    payload.ctx.fnId,
                    payload.ctx.attempt
            );

            Object result = function.call(ctx, client, requestBody);

            Object body = null;
            ResultStatusCode statusCode;

            if (result instanceof StepResult stepResult) {
                statusCode = stepResult.getStatusCode();

                if (STEP_TERMINAL_STATUS_CODES.contains(statusCode)) {
                    body = List.of(stepResult);
                }

                if (statusCode == ResultStatusCode.FunctionComplete) {
                    body = stepResult.getData();
                }
            } else if (result instanceof StepOptions stepOptions) {
                statusCode = stepOptions.getStatusCode();
                body = List.of(stepOptions);
            } else {
                throw new IllegalStateException("Unexpected function result type");
            }

            return new CommResponse(
                    parseRequestBody(body),
                    statusCode,
                    headers
            );
        } catch (Exception e) {
            RetryDecision retryDecision = RetryDecision.fromException(e);

            ResultStatusCode statusCode = retryDecision.shouldRetry()
                    ? ResultStatusCode.RetriableError
                    : ResultStatusCode.NonRetriableError;

            String stack = Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));

            CommError err = new CommError(
                    e.toString(),
                    e.getMessage(),
                    stack
            );

            Map<String, String> responseHeaders = new HashMap<>(headers);
            responseHeaders.putAll(retryDecision.getHeaders());

            return new CommResponse(
                    parseRequestBody(err),
                    statusCode,
                    responseHeaders
            );
        }
    }

    private String parseRequestBody(Object requestBody) {
        try {
            return OBJECT_MAPPER.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

    private String serializePayload(Object payload) {
        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            System.out.println(e);
            return "{ \"message\": \"failed serialization\" }";
        }
    }

    private List<InternalFunctionConfig> getFunctionConfigs(String origin) {
        List<InternalFunctionConfig> configs = new ArrayList<>();

        for (InternalInngestFunction function : functions.values()) {
            configs.add(function.getFunctionConfig(getServeUrl(origin), client));
        }

        return configs;
    }

    public String register(
            String origin,
            String syncId
    ) throws IOException {
        String registrationUrl = config.baseUrl() + "/fn/register";
        RegistrationRequestPayload requestPayload = getRegistrationRequestPayload(origin);

        HttpClient httpClient = client.getHttpClient();

        RequestConfig authorizationHeaderRequestConfig = null;
        if (config.getClient().getEnv() != InngestEnv.Dev) {
            authorizationHeaderRequestConfig = new RequestConfig(
                    getAuthorizationHeader(config.signingKey())
            );
        }

        Map<String, String> queryParams = syncId != null
                ? Map.of(InngestQueryParamKey.SyncId.getValue(), syncId)
                : Collections.emptyMap();

        Object request = httpClient.build(
                registrationUrl,
                requestPayload,
                queryParams,
                authorizationHeaderRequestConfig
        );

        httpClient.send(request, response -> {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return null;
        });

        return parseRequestBody(Collections.emptyMap());
    }

    public String introspect(
            String signature,
            String requestBody,
            String serverKind
    ) {
        InsecureIntrospection insecureIntrospection = new InsecureIntrospection(
                functions.size(),
                Environment.isInngestEventKeySet(client.getEventKey()),
                config.hasSigningKey(),
                client.getEnv() == InngestEnv.Dev ? "dev" : "cloud"
        );

        Object responsePayload;

        if (client.getEnv() == InngestEnv.Dev) {
            responsePayload = insecureIntrospection;
        } else {
            try {
                SigningKeyUtil.checkHeadersAndValidateSignature(
                        signature,
                        requestBody,
                        serverKind,
                        config
                );

                responsePayload = new SecureIntrospection(
                        functions.size(),
                        Environment.isInngestEventKeySet(client.getEventKey()),
                        config.hasSigningKey(),
                        true,
                        "cloud",
                        client.getEnv().getValue(),
                        config.appId(),
                        config.baseUrl() + "/",
                        framework.getValue(),
                        Version.getVersion(),
                        "java",
                        config.servePath(),
                        config.serveOrigin(),
                        hashedSigningKey(config.signingKey()),
                        Environment.inngestEventApiBaseUrl(client.getEnv()) + "/",
                        config.hasSigningKey()
                                ? hashedEventKey(client.getEventKey())
                                : null
                );
            } catch (Exception e) {
                insecureIntrospection.setAuthenticationSucceeded(false);
                responsePayload = insecureIntrospection;
            }
        }

        return serializePayload(responsePayload);
    }

    private RegistrationRequestPayload getRegistrationRequestPayload(String origin) {
        return new RegistrationRequestPayload(
                config.appId(),
                framework.getValue(),
                getFunctionConfigs(origin),
                "java:v" + Version.getVersion(),
                getServeUrl(origin),
                "0.1"
        );
    }

    private String getServeUrl(String origin) {
        String serveOrigin = config.serveOrigin() != null
                ? config.serveOrigin()
                : origin;

        String servePath = config.servePath() != null
                ? config.servePath()
                : "/api/inngest";

        return serveOrigin + servePath;
    }

    private String hashedEventKey(String eventKey) {
        if (!Environment.isInngestEventKeySet(eventKey)) {
            return null;
        }

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(eventKey.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash event key", e);
        }
    }
}
