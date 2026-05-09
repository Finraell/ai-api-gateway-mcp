package com.dsushkov.aiapigateway.risk;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RiskDecisionMapper {
    private static final String SEP = "|";

    public RiskDecisionEntity toEntity(RiskScoreResponse response) {
        RiskDecisionEntity entity = new RiskDecisionEntity();
        entity.setDecisionId(response.decisionId());
        entity.setCustomerId(response.customerId());
        entity.setTransactionId(response.transactionId());
        entity.setDecision(response.decision());
        entity.setRiskScore(response.riskScore());
        entity.setReasons(response.reasons());
        entity.setSignals(response.signals().stream().map(this::encodeSignal).toList());
        entity.setRecommendation(response.recommendation());
        entity.setModelVersion(response.modelVersion());
        entity.setCreatedAt(response.createdAt());
        entity.setDurationMs(response.durationMs());
        return entity;
    }

    public RiskScoreResponse toResponse(RiskDecisionEntity entity) {
        return new RiskScoreResponse(
                entity.getDecisionId(),
                entity.getCustomerId(),
                entity.getTransactionId(),
                entity.getDecision(),
                entity.getRiskScore(),
                List.copyOf(entity.getReasons()),
                entity.getSignals().stream().map(this::decodeSignal).toList(),
                entity.getRecommendation(),
                entity.getModelVersion(),
                entity.getCreatedAt(),
                entity.getDurationMs()
        );
    }

    private String encodeSignal(RiskSignal signal) {
        return signal.code() + SEP + signal.severity() + SEP + signal.points() + SEP + signal.explanation().replace(SEP, "/");
    }

    private RiskSignal decodeSignal(String raw) {
        String[] parts = raw.split("\\|", 4);
        if (parts.length != 4) {
            return new RiskSignal("UNKNOWN", RiskSeverity.LOW, 0, raw);
        }
        return new RiskSignal(parts[0], RiskSeverity.valueOf(parts[1]), Integer.parseInt(parts[2]), parts[3]);
    }
}
