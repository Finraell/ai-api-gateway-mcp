package com.dsushkov.aiapigateway.observability;

import org.slf4j.MDC;

public final class CorrelationIds {
    private CorrelationIds() {
    }

    public static String current() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        return correlationId == null ? "unknown" : correlationId;
    }
}
