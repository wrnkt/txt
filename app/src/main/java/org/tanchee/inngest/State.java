package org.tanchee.inngest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class State {

    public static final class StateNotFound extends RuntimeException {
        public StateNotFound() {
            super("State not found for id");
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String payloadJson;
    private final Map<String, Integer> stepIdsToNextStepNumber = new HashMap<>();
    private final Set<String> stepIds = new HashSet<>();

    public State(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public String getHashFromId(String id) {
        String idToHash = findNextAvailableStepId(id);
        stepIds.add(idToHash);

        try {
            byte[] bytes = idToHash.getBytes(StandardCharsets.UTF_8);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashedBytes = digest.digest(bytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append("%02x".formatted(b));
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash step id", e);
        }
    }

    private String findNextAvailableStepId(String id) {
        if (!stepIds.contains(id)) {
            return id;
        }

        int stepNumber = stepIdsToNextStepNumber.getOrDefault(id, 1);

        while (stepIds.contains("%s:%d".formatted(id, stepNumber))) {
            stepNumber++;
        }

        stepIdsToNextStepNumber.put(id, stepNumber + 1);

        return "%s:%d".formatted(id, stepNumber);
    }

    public <T> T getState(String hashedId, Class<T> type) {
        return getState(hashedId, type, "data");
    }

    public <T> T getState(String hashedId, Class<T> type, String fieldName) {
        try {
            JsonNode node = MAPPER.readTree(payloadJson);
            JsonNode steps = node.path("steps");
            JsonNode stepResult = steps.get(hashedId);

            if (stepResult == null) {
                throw new StateNotFound();
            }

            if (stepResult.has(fieldName)) {
                return deserializeStepData(stepResult.get(fieldName), type);
            }

            if (stepResult.has("error")) {
                throw MAPPER.treeToValue(stepResult.get("error"), StepError.class);
            }

            if (stepResult instanceof NullNode) {
                return null;
            }

            throw new IllegalStateException("Unexpected step data structure");
        } catch (StepError e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get state", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeStepData(JsonNode serializedStepData, Class<T> type) {
        try {
            if (serializedStepData == null
                || !serializedStepData.isObject()
                || !serializedStepData.has("class")) {
                return MAPPER.treeToValue(serializedStepData, type);
            }

            ObjectNode writableJson = (ObjectNode) serializedStepData;
            String className = writableJson.remove("class").asText();

            return (T) MAPPER.treeToValue(
                writableJson,
                Class.forName(className)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize step data", e);
        }
    }
}
