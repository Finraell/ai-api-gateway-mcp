package com.dsushkov.aiapigateway.observability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class StructuredLogWriter {
    private final ObjectMapper objectMapper;

    public StructuredLogWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void info(Logger logger, String eventName, Map<String, ?> fields) {
        logger.info(toJson(eventName, fields));
    }

    public void warn(Logger logger, String eventName, Map<String, ?> fields) {
        logger.warn(toJson(eventName, fields));
    }

    private String toJson(String eventName, Map<String, ?> fields) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", eventName);
        payload.put("timestamp", Instant.now().toString());
        payload.put("correlationId", CorrelationIds.current());
        payload.putAll(fields);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return "{\"event\":\"" + eventName + "\",\"serializationError\":\"" + ex.getClass().getSimpleName() + "\"}";
        }
    }
}
