package com.dsushkov.aiapigateway.mcp;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RiskScoringToolDescriptorTest {
    @Test
    @SuppressWarnings("unchecked")
    void exposesAllRiskScoringInputFields() {
        RiskScoringTool tool = new RiskScoringTool(null, JsonMapper.builder().findAndAddModules().build());

        ToolDescriptor descriptor = tool.descriptor();
        Map<String, Object> properties = (Map<String, Object>) descriptor.inputSchema().get("properties");

        assertThat(descriptor.name()).isEqualTo("risk.score.transaction");
        assertThat(properties)
                .containsKeys(
                        "customerId",
                        "transactionId",
                        "transactionAmount",
                        "currency",
                        "countryCode",
                        "merchantCategory",
                        "accountAgeDays",
                        "failedLoginCount",
                        "newDevice",
                        "deviceTrustScore",
                        "velocity30m",
                        "previousChargebacks",
                        "ipRiskScore"
                )
                .hasSize(13);
    }
}
