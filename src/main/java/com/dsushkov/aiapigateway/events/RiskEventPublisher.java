package com.dsushkov.aiapigateway.events;

import com.dsushkov.aiapigateway.risk.RiskScoreResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RiskEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(RiskEventPublisher.class);

    private final ObjectProvider<KafkaTemplate<String, RiskDecisionCreatedEvent>> kafkaTemplateProvider;
    private final boolean eventsEnabled;
    private final String topic;

    public RiskEventPublisher(
            ObjectProvider<KafkaTemplate<String, RiskDecisionCreatedEvent>> kafkaTemplateProvider,
            @Value("${gateway.events.enabled:false}") boolean eventsEnabled,
            @Value("${gateway.events.topic:risk.decision.created}") String topic
    ) {
        this.kafkaTemplateProvider = kafkaTemplateProvider;
        this.eventsEnabled = eventsEnabled;
        this.topic = topic;
    }

    public void publish(RiskScoreResponse response) {
        if (!eventsEnabled) {
            log.info("risk_event_publish_skipped decisionId={} reason=events_disabled", response.decisionId());
            return;
        }

        KafkaTemplate<String, RiskDecisionCreatedEvent> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            log.warn("risk_event_publish_skipped decisionId={} reason=kafka_template_not_configured", response.decisionId());
            return;
        }

        RiskDecisionCreatedEvent event = new RiskDecisionCreatedEvent(
                response.decisionId(),
                response.customerId(),
                response.transactionId(),
                response.decision(),
                response.riskScore(),
                response.modelVersion(),
                response.createdAt()
        );
        try {
            kafkaTemplate.send(topic, response.decisionId(), event);
            log.info("risk_event_published decisionId={} topic={}", response.decisionId(), topic);
        } catch (RuntimeException ex) {
            log.warn("risk_event_publish_failed decisionId={} reason={}", response.decisionId(), ex.getMessage());
        }
    }
}
