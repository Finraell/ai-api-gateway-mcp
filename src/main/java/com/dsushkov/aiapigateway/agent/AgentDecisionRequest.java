package com.dsushkov.aiapigateway.agent;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record AgentDecisionRequest(
        @NotBlank String userPrompt,
        Map<String, Object> context
) {
}
