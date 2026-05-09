package com.dsushkov.aiapigateway.risk;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class RiskScoringEngine {
    private final RiskPolicy policy;

    public RiskScoringEngine(RiskPolicy policy) {
        this.policy = policy;
    }

    public RiskScoreResponse score(RiskScoreRequest request) {
        long started = System.nanoTime();
        List<RiskSignal> signals = new ArrayList<>();

        BigDecimal amount = request.transactionAmount();
        if (amount.compareTo(BigDecimal.valueOf(10_000)) >= 0) {
            signals.add(new RiskSignal("LARGE_TRANSACTION", RiskSeverity.HIGH, 35, "Transaction amount is above the high-value threshold."));
        } else if (amount.compareTo(BigDecimal.valueOf(2_500)) >= 0) {
            signals.add(new RiskSignal("ELEVATED_TRANSACTION", RiskSeverity.MEDIUM, 15, "Transaction amount is above the elevated review threshold."));
        }

        String countryCode = request.countryCode().toUpperCase(Locale.ROOT);
        if (policy.highRiskCountry(countryCode)) {
            signals.add(new RiskSignal("HIGH_RISK_COUNTRY", RiskSeverity.CRITICAL, 35, "Country code is classified as high risk by policy."));
        } else if (policy.elevatedRiskCountry(countryCode)) {
            signals.add(new RiskSignal("ELEVATED_RISK_COUNTRY", RiskSeverity.MEDIUM, 15, "Country code requires additional review."));
        }

        String merchantCategory = request.merchantCategory().trim().toUpperCase(Locale.ROOT);
        if (policy.highRiskMerchantCategory(merchantCategory)) {
            signals.add(new RiskSignal("HIGH_RISK_MERCHANT_CATEGORY", RiskSeverity.HIGH, 25, "Merchant category is frequently associated with account takeover or fraud workflows."));
        }

        if (request.accountAgeDays() < 7) {
            signals.add(new RiskSignal("VERY_NEW_ACCOUNT", RiskSeverity.HIGH, 25, "Account is less than 7 days old."));
        } else if (request.accountAgeDays() < 30) {
            signals.add(new RiskSignal("NEW_ACCOUNT", RiskSeverity.MEDIUM, 15, "Account is less than 30 days old."));
        }

        if (request.failedLoginCount() >= 8) {
            signals.add(new RiskSignal("LOGIN_ATTACK_PATTERN", RiskSeverity.CRITICAL, 30, "Failed login count indicates possible credential stuffing or takeover attempt."));
        } else if (request.failedLoginCount() >= 4) {
            signals.add(new RiskSignal("MULTIPLE_FAILED_LOGINS", RiskSeverity.MEDIUM, 15, "Multiple failed login attempts were observed before the transaction."));
        }

        if (request.newDevice() && request.deviceTrustScore() < 40) {
            signals.add(new RiskSignal("UNTRUSTED_NEW_DEVICE", RiskSeverity.HIGH, 25, "New device has a low trust score."));
        } else if (request.newDevice()) {
            signals.add(new RiskSignal("NEW_DEVICE", RiskSeverity.LOW, 8, "Transaction was initiated from a new device."));
        }

        if (request.velocity30m() >= 8) {
            signals.add(new RiskSignal("HIGH_VELOCITY", RiskSeverity.HIGH, 25, "High number of transactions in the last 30 minutes."));
        } else if (request.velocity30m() >= 4) {
            signals.add(new RiskSignal("ELEVATED_VELOCITY", RiskSeverity.MEDIUM, 12, "Elevated transaction velocity in the last 30 minutes."));
        }

        if (request.previousChargebacks() >= 3) {
            signals.add(new RiskSignal("CHARGEBACK_HISTORY", RiskSeverity.HIGH, 25, "Customer has multiple previous chargebacks."));
        } else if (request.previousChargebacks() > 0) {
            signals.add(new RiskSignal("PREVIOUS_CHARGEBACK", RiskSeverity.MEDIUM, 10, "Customer has at least one previous chargeback."));
        }

        if (request.ipRiskScore() >= 80) {
            signals.add(new RiskSignal("HIGH_RISK_IP", RiskSeverity.CRITICAL, 30, "IP reputation score is high risk."));
        } else if (request.ipRiskScore() >= 50) {
            signals.add(new RiskSignal("ELEVATED_RISK_IP", RiskSeverity.MEDIUM, 12, "IP reputation score is elevated."));
        }

        int score = Math.min(100, signals.stream().mapToInt(RiskSignal::points).sum());
        RiskDecision decision = decide(score);
        List<String> reasons = signals.isEmpty()
                ? List.of("No material risk signals detected")
                : signals.stream().map(RiskSignal::explanation).toList();

        return new RiskScoreResponse(
                UUID.randomUUID().toString(),
                request.customerId(),
                request.transactionId(),
                decision,
                score,
                reasons,
                List.copyOf(signals),
                recommendation(decision),
                policy.modelVersion(),
                Instant.now(),
                Math.max(1, (System.nanoTime() - started) / 1_000_000)
        );
    }

    private RiskDecision decide(int score) {
        if (score >= policy.declineThreshold()) {
            return RiskDecision.DECLINE;
        }
        if (score >= policy.reviewThreshold()) {
            return RiskDecision.REVIEW;
        }
        return RiskDecision.APPROVE;
    }

    private String recommendation(RiskDecision decision) {
        return switch (decision) {
            case APPROVE -> "Approve automatically; continue passive monitoring.";
            case REVIEW -> "Route to manual review with enhanced verification.";
            case DECLINE -> "Decline or hold transaction pending out-of-band verification.";
        };
    }
}
