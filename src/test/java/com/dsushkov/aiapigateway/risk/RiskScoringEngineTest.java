package com.dsushkov.aiapigateway.risk;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RiskScoringEngineTest {
    private final RiskScoringEngine engine = new RiskScoringEngine(new RiskPolicy("test-rules"));

    @Test
    void approvesLowRiskTransaction() {
        RiskScoreResponse response = engine.score(new RiskScoreRequest(
                "cust-1", "tx-1", BigDecimal.valueOf(42), "USD", "US", "GROCERY",
                365, 0, false, 98, 1, 0, 5
        ));

        assertThat(response.decision()).isEqualTo(RiskDecision.APPROVE);
        assertThat(response.riskScore()).isLessThan(35);
        assertThat(response.reasons()).contains("No material risk signals detected");
    }

    @Test
    void declinesHighRiskTransaction() {
        RiskScoreResponse response = engine.score(new RiskScoreRequest(
                "cust-2", "tx-2", BigDecimal.valueOf(15_000), "USD", "IR", "CRYPTO",
                2, 9, true, 10, 9, 4, 95
        ));

        assertThat(response.decision()).isEqualTo(RiskDecision.DECLINE);
        assertThat(response.riskScore()).isEqualTo(100);
        assertThat(response.signals()).extracting(RiskSignal::code)
                .contains("LARGE_TRANSACTION", "HIGH_RISK_COUNTRY", "HIGH_RISK_MERCHANT_CATEGORY", "LOGIN_ATTACK_PATTERN");
    }
}
