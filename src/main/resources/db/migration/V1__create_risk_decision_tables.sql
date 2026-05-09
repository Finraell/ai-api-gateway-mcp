CREATE TABLE IF NOT EXISTS risk_decisions (
    decision_id VARCHAR(120) PRIMARY KEY,
    customer_id VARCHAR(120) NOT NULL,
    transaction_id VARCHAR(120) NOT NULL UNIQUE,
    risk_score INTEGER NOT NULL,
    decision VARCHAR(40) NOT NULL,
    recommendation VARCHAR(1000),
    model_version VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_ms BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_risk_customer ON risk_decisions(customer_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_risk_transaction ON risk_decisions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_risk_created_at ON risk_decisions(created_at);

CREATE TABLE IF NOT EXISTS risk_decision_reasons (
    decision_id VARCHAR(120) NOT NULL,
    reason VARCHAR(1000),
    CONSTRAINT fk_risk_decision_reasons_decision
        FOREIGN KEY (decision_id) REFERENCES risk_decisions(decision_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS risk_decision_signals (
    decision_id VARCHAR(120) NOT NULL,
    signal VARCHAR(1000),
    CONSTRAINT fk_risk_decision_signals_decision
        FOREIGN KEY (decision_id) REFERENCES risk_decisions(decision_id) ON DELETE CASCADE
);
