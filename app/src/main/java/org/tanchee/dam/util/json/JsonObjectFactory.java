package org.tanchee.dam.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class JsonObjectFactory {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Object> createObjectsFromJson(String jsonArray) throws Exception {
        System.out.println("createObjectsFromJson");
        List<Object> result = new ArrayList<>();
        JsonNode arrayNode = mapper.readTree(jsonArray);
        if (!arrayNode.isArray()) {
            throw new IllegalArgumentException("Expected a JSON array");
        }

        for (JsonNode node : arrayNode) {
            Object obj = createObjectFromJsonNode(node);
            result.add(obj);
        }
        return result;
    }

    private static Object createObjectFromJsonNode(JsonNode node) throws Exception {
        System.out.println("createObjectsFromJsonNode");
        String className = node.get("className").asText();
        Class<?> clazz = Class.forName(className);
        System.out.printf("className: %s\n", clazz.getName());

        Iterator<String> fieldNames = node.fieldNames();
        Map<String, JsonNode> params = new HashMap<>();
        while (fieldNames.hasNext()) {
            String f = fieldNames.next();
            System.out.printf("%s: %s\n", f, node.get(f));
            if (!f.equals("className")) {
                params.put(f, node.get(f));
            }
        }

        // Find a matching constructor and prepare arguments
        for (Constructor<?> ctor : clazz.getConstructors()) {
            System.out.printf("%s\n", ctor.toString());
            Class<?>[] paramTypes = ctor.getParameterTypes();
            if (paramTypes.length != params.size()) continue;

            Object[] args = new Object[paramTypes.length];
            boolean allMatched = true;
            int i = 0;
            for (Class<?> paramType : paramTypes) {
                String paramName = getParamNameByIndex(i, params);
                System.out.printf("%s[%d]\n", paramName, i);
                if (paramName == null) {
                    allMatched = false;
                    break;
                }
                JsonNode valueNode = params.get(paramName);
                Object arg = convertValue(valueNode, paramType);
                if (arg == null) {
                    allMatched = false;
                    break;
                }
                args[i++] = arg;
            }

            if (allMatched) {
                try {
                    return ctor.newInstance(args);
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to instantiate " + className, e);
                }
            }
        }

        throw new RuntimeException("No suitable constructor found for class " + className);
    }

    // Helper: match param names in order
    private static String getParamNameByIndex(int index, Map<String, JsonNode> params) {
        return params.keySet().stream().skip(index).findFirst().orElse(null);
    }

    // Helper: convert JSON node to param type (handles String and Enum here)
    private static Object convertValue(JsonNode node, Class<?> targetType) {
        if (targetType == String.class) {
            return node.asText();
        } else if (targetType.isEnum()) {
            String enumValue = node.asText();
            Object[] constants = targetType.getEnumConstants();
            for (Object c : constants) {
                if (c.toString().equalsIgnoreCase(enumValue)) {
                    return c;
                }
            }
            return null;
        } else if (targetType == int.class || targetType == Integer.class) {
            return node.asInt();
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return node.asBoolean();
        }
        // Extend with other types if needed
        return null;
    }
}

