package com.julianduru.cdc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * created by julian
 */
public class JSONUtil {

    static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
    }


    public static String asJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }


    public static String asJsonString(Object obj, String defaultString) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return defaultString;
        }
    }


    public static <T> T extractPayload(String payloadString, Class<T> klass) throws IOException {
        try {
            if (klass == String.class) {
                return (T) payloadString;
            }

            return objectMapper.readValue(payloadString, klass);
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot deserialize payload to " + klass.getSimpleName(), e);
        }
    }


    public static <T> T fromJsonString(String json, Class<T> klass) throws IOException {
        return objectMapper.readValue(json, klass);
    }


    public static Map<String, String> readJSONMap(String jsonSource) {
        if (!StringUtils.hasText(jsonSource)) {
            return new HashMap<>();
        }

        Map<String, Object> map = new JSONObject(jsonSource).toMap();
        return map.entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<String, String>(e.getKey(), e.getValue().toString()) {})
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    public static Payload readPayload(String str) throws IOException {
        JsonNode node = objectMapper.readTree(str);
        JsonNode payloadNode = node.path("payload");
        if (payloadNode.isMissingNode()) {
            throw new JSONException("Payload is missing");
        }

        return objectMapper.treeToValue(payloadNode, Payload.class);
    }


}
