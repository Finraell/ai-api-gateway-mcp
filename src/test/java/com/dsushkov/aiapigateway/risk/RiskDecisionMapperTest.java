package com.dsushkov.aiapigateway.risk;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskDecisionMapperTest {
    @Test
    void roundTripsEntity() {
        RiskDecisionMapper mapper = new RiskDecisionMapper();
        RiskScoreResponse original = new RiskScoreResponse(
                "decision-1",
                "cust-1",
                "tx-1",
                RiskDecision.REVIEW,
                55,
                List.of("reason"),
                List.of(new RiskSignal("TEST", RiskSeverity.MEDIUM, 55, "explanation")),
                "review",
                "rules-test",
                Instant.now(),
                7
        );

        RiskScoreResponse mapped = mapper.toResponse(mapper.toEntity(original));

        assertThat(mapped.decisionId()).isEqualTo(original.decisionId());
        assertThat(mapped.signals()).hasSize(1);
        assertThat(mapped.signals().getFirst().code()).isEqualTo("TEST");
    }
}
