package com.dsushkov.aiapigateway.mcp;

import com.dsushkov.aiapigateway.risk.RiskDecisionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DecisionLookupTool implements GatewayTool {
    private final RiskDecisionService riskDecisionService;

    public DecisionLookupTool(RiskDecisionService riskDecisionService) {
        this.riskDecisionService = riskDecisionService;
    }

    @Override
    public ToolDescriptor descriptor() {
        return new ToolDescriptor(
                "risk.lookup.decision",
                "Retrieves a previously persisted risk decision by decision id.",
                ToolSchemas.object(Map.of("decisionId", ToolSchemas.string("Decision id returned by risk.score.transaction")), List.of("decisionId"))
        );
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        Object value = arguments.get("decisionId");
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("decisionId is required");
        }
        return riskDecisionService.findByDecisionId(value.toString());
    }
}
