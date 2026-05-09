package com.dsushkov.aiapigateway.audit;

import com.dsushkov.aiapigateway.observability.CorrelationIds;
import com.dsushkov.aiapigateway.risk.RiskScoreResponse;
import com.dsushkov.aiapigateway.risk.RiskSignal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditEventService {
    private final AuditEventRepository repository;
    private final ObjectMapper objectMapper;

    public AuditEventService(AuditEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRiskDecisionCreated(RiskScoreResponse response, boolean cacheHit) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("decisionId", response.decisionId());
        payload.put("customerId", response.customerId());
        payload.put("transactionId", response.transactionId());
        payload.put("decision", response.decision().name());
        payload.put("riskScore", response.riskScore());
        payload.put("modelVersion", response.modelVersion());
        payload.put("signalCodes", response.signals().stream().map(RiskSignal::code).toList());
        payload.put("cacheHit", cacheHit);
        save("risk_decision_created", response.decisionId(), payload);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordMcpToolCall(String toolName, boolean success, long durationMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tool", toolName);
        payload.put("success", success);
        payload.put("durationMs", durationMs);
        save("mcp_tool_called", toolName, payload);
    }

    private void save(String eventType, String aggregateId, Map<String, Object> payload) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.setEventId(UUID.randomUUID().toString());
        entity.setEventType(eventType);
        entity.setAggregateId(aggregateId);
        entity.setCorrelationId(CorrelationIds.current());
        entity.setPrincipal(currentPrincipal());
        entity.setPayloadJson(toJson(payload));
        entity.setCreatedAt(Instant.now());
        repository.save(entity);
    }

    private String currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "anonymous";
        }
        return authentication.getName();
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return "{\"serializationError\":\"" + ex.getClass().getSimpleName() + "\"}";
        }
    }
}
