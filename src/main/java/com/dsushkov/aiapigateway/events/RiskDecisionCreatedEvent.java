package com.dsushkov.aiapigateway.events;

import com.dsushkov.aiapigateway.risk.RiskDecision;

import java.time.Instant;

public record RiskDecisionCreatedEvent(
        String decisionId,
        String customerId,
        String transactionId,
        RiskDecision decision,
        int riskScore,
        String modelVersion,
        Instant createdAt
) {
}
