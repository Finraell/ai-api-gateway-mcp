package com.dsushkov.aiapigateway.risk;

import java.time.Instant;
import java.util.List;

public record RiskScoreResponse(
        String decisionId,
        String customerId,
        String transactionId,
        RiskDecision decision,
        int riskScore,
        List<String> reasons,
        List<RiskSignal> signals,
        String recommendation,
        String modelVersion,
        Instant createdAt,
        long durationMs
) {
}
