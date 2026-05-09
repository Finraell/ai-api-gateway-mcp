package com.dsushkov.aiapigateway.mcp;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class MerchantRiskTool implements GatewayTool {
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

    @Override
    public ToolDescriptor descriptor() {
        return new ToolDescriptor(
                "merchant.risk.lookup",
                "Returns a simple merchant-category risk profile used by the scoring system.",
                ToolSchemas.object(Map.of("merchantCategory", ToolSchemas.string("Merchant category")), List.of("merchantCategory"))
        );
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        Object raw = arguments.get("merchantCategory");
        if (raw == null || raw.toString().isBlank()) {
            throw new IllegalArgumentException("merchantCategory is required");
        }
        String category = raw.toString().trim().toUpperCase(Locale.ROOT);
        int baseRisk = CATEGORY_BASE_RISK.getOrDefault(category, 25);
        return Map.of(
                "merchantCategory", category,
                "baseRisk", baseRisk,
                "tier", baseRisk >= 70 ? "HIGH" : baseRisk >= 40 ? "MEDIUM" : "LOW"
        );
    }
}
