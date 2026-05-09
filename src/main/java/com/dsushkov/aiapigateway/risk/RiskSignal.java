package com.dsushkov.aiapigateway.risk;

public record RiskSignal(
        String code,
        RiskSeverity severity,
        int points,
        String explanation
) {
}
