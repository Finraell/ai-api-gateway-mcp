package com.dsushkov.aiapigateway.mcp;

import com.dsushkov.aiapigateway.risk.RiskDecisionService;
import com.dsushkov.aiapigateway.risk.RiskScoreRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RiskScoringTool implements GatewayTool {
    private final RiskDecisionService riskDecisionService;
    private final ObjectMapper objectMapper;

    public RiskScoringTool(RiskDecisionService riskDecisionService, ObjectMapper objectMapper) {
        this.riskDecisionService = riskDecisionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ToolDescriptor descriptor() {
        return new ToolDescriptor(
                "risk.score.transaction",
                "Scores a transaction, persists the decision, and returns explainable risk signals.",
                ToolSchemas.object(Map.<String, Object>ofEntries(
                        Map.entry("customerId", ToolSchemas.string("Customer id")),
                        Map.entry("transactionId", ToolSchemas.string("Transaction id")),
                        Map.entry("transactionAmount", ToolSchemas.number("Transaction amount")),
                        Map.entry("currency", ToolSchemas.string("ISO currency code")),
                        Map.entry("countryCode", ToolSchemas.string("ISO country code")),
                        Map.entry("merchantCategory", ToolSchemas.string("Merchant category")),
                        Map.entry("accountAgeDays", ToolSchemas.integer("Account age in days")),
                        Map.entry("failedLoginCount", ToolSchemas.integer("Recent failed login count")),
                        Map.entry("newDevice", ToolSchemas.bool("Whether the transaction uses a new device")),
                        Map.entry("deviceTrustScore", ToolSchemas.integer("Device trust score from 0 to 100")),
                        Map.entry("velocity30m", ToolSchemas.integer("Number of transactions in last 30 minutes")),
                        Map.entry("previousChargebacks", ToolSchemas.integer("Prior chargeback count")),
                        Map.entry("ipRiskScore", ToolSchemas.integer("IP risk score from 0 to 100"))
                ), List.of("customerId", "transactionId", "transactionAmount", "currency", "countryCode", "merchantCategory"))
        );
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        RiskScoreRequest request = objectMapper.convertValue(arguments, RiskScoreRequest.class);
        return riskDecisionService.evaluate(request);
    }
}
