package com.dsushkov.aiapigateway.risk;

import com.dsushkov.aiapigateway.audit.AuditEventService;
import com.dsushkov.aiapigateway.events.RiskEventPublisher;
import com.dsushkov.aiapigateway.observability.RiskMetrics;
import com.dsushkov.aiapigateway.observability.StructuredLogWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RiskDecisionService {
    private static final Logger log = LoggerFactory.getLogger(RiskDecisionService.class);

    private final RiskScoringEngine engine;
    private final RiskDecisionRepository repository;
    private final RiskDecisionMapper mapper;
    private final RiskEventPublisher eventPublisher;
    private final RiskMetrics riskMetrics;
    private final AuditEventService auditEventService;
    private final StructuredLogWriter structuredLogWriter;

    public RiskDecisionService(
            RiskScoringEngine engine,
            RiskDecisionRepository repository,
            RiskDecisionMapper mapper,
            RiskEventPublisher eventPublisher,
            RiskMetrics riskMetrics,
            AuditEventService auditEventService,
            StructuredLogWriter structuredLogWriter
    ) {
        this.engine = engine;
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
        this.riskMetrics = riskMetrics;
        this.auditEventService = auditEventService;
        this.structuredLogWriter = structuredLogWriter;
    }

    @Transactional
    public RiskScoreResponse evaluate(RiskScoreRequest request) {
        Instant start = Instant.now();
        RiskScoreResponse existing = repository.findByTransactionId(request.transactionId())
                .map(mapper::toResponse)
                .orElse(null);
        if (existing != null) {
            Duration elapsed = Duration.between(start, Instant.now());
            riskMetrics.recordDecision(existing, true, elapsed);
            auditEventService.recordRiskDecisionCreated(existing, true);
            structuredLogWriter.info(log, "risk_decision_cache_hit", fields(existing, true, elapsed));
            return existing;
        }

        RiskScoreResponse response = engine.score(request);
        repository.save(mapper.toEntity(response));

        Duration elapsed = Duration.between(start, Instant.now());
        riskMetrics.recordDecision(response, false, elapsed);
        auditEventService.recordRiskDecisionCreated(response, false);
        structuredLogWriter.info(log, "risk_decision_created", fields(response, false, elapsed));

        eventPublisher.publish(response);
        return response;
    }

    @Transactional(readOnly = true)
    public RiskScoreResponse findByDecisionId(String decisionId) {
        return repository.findById(decisionId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new DecisionNotFoundException(decisionId));
    }

    @Transactional(readOnly = true)
    public List<RiskScoreResponse> findRecentByCustomerId(String customerId) {
        return repository.findTop20ByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    private Map<String, Object> fields(RiskScoreResponse response, boolean cacheHit, Duration elapsed) {
        String signalCodes = response.signals().stream()
                .map(RiskSignal::code)
                .collect(Collectors.joining(","));
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("decisionId", response.decisionId());
        fields.put("transactionId", response.transactionId());
        fields.put("customerId", response.customerId());
        fields.put("decision", response.decision().name());
        fields.put("riskScore", response.riskScore());
        fields.put("signalCount", response.signals().size());
        fields.put("signalCodes", signalCodes);
        fields.put("modelVersion", response.modelVersion());
        fields.put("cacheHit", cacheHit);
        fields.put("durationMs", elapsed.toMillis());
        return fields;
    }
}
