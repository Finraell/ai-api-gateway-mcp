package com.dsushkov.aiapigateway.risk;

import com.dsushkov.aiapigateway.audit.AuditEventService;
import com.dsushkov.aiapigateway.events.RiskEventPublisher;
import com.dsushkov.aiapigateway.observability.RiskMetrics;
import com.dsushkov.aiapigateway.observability.StructuredLogWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RiskDecisionServiceLoggingTest {
    @Test
    void evaluatesAndPublishesDecision() {
        RiskScoringEngine engine = new RiskScoringEngine(new RiskPolicy("rules-test"));
        RiskDecisionRepository repository = mock(RiskDecisionRepository.class);
        RiskDecisionMapper mapper = new RiskDecisionMapper();
        RiskEventPublisher publisher = mock(RiskEventPublisher.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AuditEventService auditEventService = mock(AuditEventService.class);
        StructuredLogWriter structuredLogWriter = new StructuredLogWriter(new ObjectMapper());
        RiskDecisionService service = new RiskDecisionService(
                engine,
                repository,
                mapper,
                publisher,
                new RiskMetrics(meterRegistry),
                auditEventService,
                structuredLogWriter
        );

        when(repository.findByTransactionId("tx-log-test")).thenReturn(Optional.empty());
        when(repository.save(any(RiskDecisionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RiskScoreResponse response = service.evaluate(new RiskScoreRequest(
                "cust-log-test",
                "tx-log-test",
                BigDecimal.valueOf(12500),
                "USD",
                "US",
                "WIRE_TRANSFER",
                14,
                5,
                true,
                35,
                6,
                1,
                62
        ));

        assertThat(response.decision()).isEqualTo(RiskDecision.DECLINE);
        assertThat(response.riskScore()).isEqualTo(100);
        verify(repository).save(any(RiskDecisionEntity.class));
        verify(publisher).publish(response);
    }
}
