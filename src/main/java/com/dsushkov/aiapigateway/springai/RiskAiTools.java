package com.dsushkov.aiapigateway.springai;

import com.dsushkov.aiapigateway.audit.AuditEventService;
import com.dsushkov.aiapigateway.observability.RiskMetrics;
import com.dsushkov.aiapigateway.risk.RiskDecisionService;
import com.dsushkov.aiapigateway.risk.RiskPolicy;
import com.dsushkov.aiapigateway.risk.RiskScoreRequest;
import com.dsushkov.aiapigateway.risk.RiskScoreResponse;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class RiskAiTools {
    private static final Logger log = LoggerFactory.getLogger(RiskAiTools.class);
    private static final Map<String, Integer> CATEGORY_BASE_RISK = Map.of(
            "CRYPTO", 85,
            "WIRE_TRANSFER", 75,
            "GIFT_CARD", 70,
            "MONEY_TRANSFER", 72,
            "TRAVEL", 45,
            "GROCERY", 10,
            "RESTAURANT", 20,
            "ECOMMERCE", 35
    );

    private final RiskDecisionService riskDecisionService;
    private final RiskPolicy riskPolicy;
    private final RiskMetrics riskMetrics;
    private final AuditEventService auditEventService;

    public RiskAiTools(
            RiskDecisionService riskDecisionService,
            RiskPolicy riskPolicy,
            RiskMetrics riskMetrics,
            AuditEventService auditEventService
    ) {
        this.riskDecisionService = riskDecisionService;
        this.riskPolicy = riskPolicy;
        this.riskMetrics = riskMetrics;
        this.auditEventService = auditEventService;
    }

    @McpTool(
            name = "risk_score_transaction",
            description = "Score a financial transaction, persist the decision, and return explainable risk signals. Deterministic and audit-friendly; no LLM provider API key is required.",
            annotations = @McpTool.McpAnnotations(
                    title = "Score Transaction Risk",
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = false
            )
    )
    public RiskScoreResponse scoreTransaction(
            @McpToolParam(description = "Customer id", required = true) String customerId,
            @McpToolParam(description = "Unique transaction id. Reusing an id returns the existing persisted decision.", required = true) String transactionId,
            @McpToolParam(description = "Transaction amount", required = true) BigDecimal transactionAmount,
            @McpToolParam(description = "ISO 4217 currency code, for example USD", required = true) String currency,
            @McpToolParam(description = "ISO 3166-1 alpha-2 country code, for example US", required = true) String countryCode,
            @McpToolParam(description = "Merchant category, for example GROCERY, ECOMMERCE, WIRE_TRANSFER, CRYPTO", required = true) String merchantCategory,
            @McpToolParam(description = "Account age in days", required = true) int accountAgeDays,
            @McpToolParam(description = "Recent failed login count", required = true) int failedLoginCount,
            @McpToolParam(description = "Whether the transaction is from a new device", required = true) boolean newDevice,
            @McpToolParam(description = "Device trust score from 0 to 100", required = true) int deviceTrustScore,
            @McpToolParam(description = "Number of transactions in the last 30 minutes", required = true) int velocity30m,
            @McpToolParam(description = "Prior chargeback count", required = true) int previousChargebacks,
            @McpToolParam(description = "IP reputation score from 0 to 100", required = true) int ipRiskScore
    ) {
        return recordMcpToolCall("risk_score_transaction", () -> riskDecisionService.evaluate(new RiskScoreRequest(
                customerId,
                transactionId,
                transactionAmount,
                currency,
                countryCode,
                merchantCategory,
                accountAgeDays,
                failedLoginCount,
                newDevice,
                deviceTrustScore,
                velocity30m,
                previousChargebacks,
                ipRiskScore
        )));
    }

    @McpTool(
            name = "risk_lookup_decision",
            description = "Look up a previously persisted risk decision by decision id.",
            annotations = @McpTool.McpAnnotations(
                    title = "Lookup Risk Decision",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true
            )
    )
    public RiskScoreResponse lookupDecision(
            @McpToolParam(description = "Decision id returned by risk_score_transaction", required = true) String decisionId
    ) {
        return recordMcpToolCall("risk_lookup_decision", () -> riskDecisionService.findByDecisionId(decisionId));
    }

    @McpTool(
            name = "risk_explain_policy",
            description = "Explain active risk thresholds, high-risk merchant categories, high-risk countries, and model version.",
            annotations = @McpTool.McpAnnotations(
                    title = "Explain Risk Policy",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true
            )
    )
    public Map<String, Object> explainPolicy(
            @McpToolParam(description = "Policy topic to explain, for example thresholds, countries, merchants, or overview", required = false) String topic
    ) {
        return recordMcpToolCall("risk_explain_policy", () -> Map.of(
                "topic", topic == null || topic.isBlank() ? "overview" : topic,
                "modelVersion", riskPolicy.modelVersion(),
                "reviewThreshold", riskPolicy.reviewThreshold(),
                "declineThreshold", riskPolicy.declineThreshold(),
                "highRiskMerchantCategories", riskPolicy.highRiskCategories(),
                "notes", List.of(
                        "Rules are deterministic and explainable by design.",
                        "Spring AI MCP exposes these backend capabilities as tools without requiring an OpenAI, Claude, or other model-provider API key.",
                        "A model or MCP client can invoke these tools, but the approval logic remains in audited backend code."
                )
        ));
    }

    @McpTool(
            name = "merchant_risk_lookup",
            description = "Return a simple merchant-category risk profile used by the scoring system.",
            annotations = @McpTool.McpAnnotations(
                    title = "Lookup Merchant Risk",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true
            )
    )
    public Map<String, Object> lookupMerchantRisk(
            @McpToolParam(description = "Merchant category, for example GROCERY, ECOMMERCE, WIRE_TRANSFER, CRYPTO", required = true) String merchantCategory
    ) {
        return recordMcpToolCall("merchant_risk_lookup", () -> {
            if (merchantCategory == null || merchantCategory.isBlank()) {
                throw new IllegalArgumentException("merchantCategory is required");
            }
            String category = merchantCategory.trim().toUpperCase(Locale.ROOT);
            int baseRisk = CATEGORY_BASE_RISK.getOrDefault(category, 25);
            return Map.of(
                    "merchantCategory", category,
                    "baseRisk", baseRisk,
                    "tier", baseRisk >= 70 ? "HIGH" : baseRisk >= 40 ? "MEDIUM" : "LOW"
            );
        });
    }

    private <T> T recordMcpToolCall(String toolName, McpToolOperation<T> operation) {
        Instant start = Instant.now();
        boolean success = false;
        try {
            T result = operation.execute();
            success = true;
            return result;
        } finally {
            Duration elapsed = Duration.between(start, Instant.now());
            riskMetrics.recordMcpToolCall(toolName, success, elapsed);
            auditEventService.recordMcpToolCall(toolName, success, elapsed.toMillis());
            log.info("{\"event\":\"mcp_tool_called\",\"tool\":\"{}\",\"status\":\"{}\",\"durationMs\":{}}",
                    toolName, success ? "success" : "error", elapsed.toMillis());
        }
    }

    @FunctionalInterface
    private interface McpToolOperation<T> {
        T execute();
    }
}
