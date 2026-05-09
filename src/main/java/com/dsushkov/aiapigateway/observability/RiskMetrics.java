package com.dsushkov.aiapigateway.observability;

import com.dsushkov.aiapigateway.risk.RiskScoreResponse;
import com.dsushkov.aiapigateway.risk.RiskSignal;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RiskMetrics {
    private final MeterRegistry registry;

    public RiskMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordDecision(RiskScoreResponse response, boolean cacheHit, Duration elapsed) {
        String decision = response.decision().name();
        String modelVersion = safe(response.modelVersion());
        registry.counter("risk_decisions_total",
                        "decision", decision,
                        "model_version", modelVersion,
                        "cache_hit", Boolean.toString(cacheHit))
                .increment();
        registry.summary("risk_decision_score",
                        "decision", decision,
                        "model_version", modelVersion)
                .record(response.riskScore());
        Timer.builder("risk_decision_duration_seconds")
                .description("End-to-end risk decision duration")
                .tag("decision", decision)
                .tag("model_version", modelVersion)
                .tag("cache_hit", Boolean.toString(cacheHit))
                .publishPercentileHistogram()
                .register(registry)
                .record(elapsed);
        for (RiskSignal signal : response.signals()) {
            registry.counter("risk_decision_signals_total",
                            "decision", decision,
                            "signal", safe(signal.code()),
                            "severity", signal.severity().name(),
                            "model_version", modelVersion)
                    .increment();
        }
    }

    public void recordMcpToolCall(String toolName, boolean success, Duration elapsed) {
        String status = success ? "success" : "error";
        registry.counter("mcp_tool_calls", "tool", safe(toolName), "status", status).increment();
        Timer.builder("mcp_tool_call_duration_seconds")
                .description("MCP tool execution duration")
                .tag("tool", safe(toolName))
                .tag("status", status)
                .publishPercentileHistogram()
                .register(registry)
                .record(elapsed);
    }

    public void recordRateLimitDecision(String path, boolean allowed) {
        registry.counter("gateway_rate_limit_requests_total",
                        "path", sanitizePath(path),
                        "result", allowed ? "allowed" : "blocked")
                .increment();
    }

    private String safe(String raw) {
        return raw == null || raw.isBlank() ? "unknown" : raw;
    }

    private String sanitizePath(String path) {
        if (path == null || path.isBlank()) {
            return "unknown";
        }
        if (path.startsWith("/api/v1/risk")) {
            return "/api/v1/risk/**";
        }
        if (path.startsWith("/api/v1/tools")) {
            return "/api/v1/tools/**";
        }
        if (path.startsWith("/api/v1/agent")) {
            return "/api/v1/agent/**";
        }
        if (path.startsWith("/mcp")) {
            return "/mcp";
        }
        return path;
    }
}
