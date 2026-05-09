package com.dsushkov.aiapigateway.mcp;

import com.dsushkov.aiapigateway.risk.RiskPolicy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PolicyExplainTool implements GatewayTool {
    private final RiskPolicy riskPolicy;

    public PolicyExplainTool(RiskPolicy riskPolicy) {
        this.riskPolicy = riskPolicy;
    }

    @Override
    public ToolDescriptor descriptor() {
        return new ToolDescriptor(
                "risk.explain.policy",
                "Explains policy thresholds, high-risk categories, and the active rules model version.",
                ToolSchemas.object(Map.of("topic", ToolSchemas.string("Optional policy topic to explain")), List.of())
        );
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        return Map.of(
                "modelVersion", riskPolicy.modelVersion(),
                "reviewThreshold", riskPolicy.reviewThreshold(),
                "declineThreshold", riskPolicy.declineThreshold(),
                "highRiskMerchantCategories", riskPolicy.highRiskCategories(),
                "notes", List.of(
                        "Rules are deterministic and explainable by design.",
                        "Production systems can replace or complement this with ML models while preserving the same API contract.",
                        "All decisions expose signals for auditability."
                )
        );
    }
}
