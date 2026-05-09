package com.dsushkov.aiapigateway.risk;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "risk_decisions", indexes = {
        @Index(name = "idx_risk_customer", columnList = "customer_id"),
        @Index(name = "idx_risk_transaction", columnList = "transaction_id", unique = true),
        @Index(name = "idx_risk_created_at", columnList = "created_at")
})
public class RiskDecisionEntity {
    @Id
    @Column(name = "decision_id", length = 120)
    private String decisionId;

    @Column(name = "customer_id", nullable = false, length = 120)
    private String customerId;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 120)
    private String transactionId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 40)
    private RiskDecision decision;

    @ElementCollection
    @CollectionTable(name = "risk_decision_reasons", joinColumns = @JoinColumn(name = "decision_id"))
    @Column(name = "reason", length = 1000)
    private List<String> reasons = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "risk_decision_signals", joinColumns = @JoinColumn(name = "decision_id"))
    @Column(name = "signal", length = 1000)
    private List<String> signals = new ArrayList<>();

    @Column(name = "recommendation", length = 1000)
    private String recommendation;

    @Column(name = "model_version", length = 120)
    private String modelVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public RiskDecision getDecision() {
        return decision;
    }

    public void setDecision(RiskDecision decision) {
        this.decision = decision;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public List<String> getSignals() {
        return signals;
    }

    public void setSignals(List<String> signals) {
        this.signals = signals;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
}
