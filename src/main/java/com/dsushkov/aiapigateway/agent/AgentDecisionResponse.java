package com.dsushkov.aiapigateway.agent;

import com.dsushkov.aiapigateway.mcp.ToolExecutionResponse;

import java.util.List;

public record AgentDecisionResponse(
        String plan,
        List<ToolExecutionResponse> toolResults,
        String finalAnswer
) {
}
