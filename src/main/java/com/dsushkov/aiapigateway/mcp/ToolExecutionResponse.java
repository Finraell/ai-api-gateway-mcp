package com.dsushkov.aiapigateway.mcp;

import java.time.Instant;

public record ToolExecutionResponse(
        String toolName,
        boolean success,
        Object result,
        String error,
        Instant executedAt
) {
    public static ToolExecutionResponse ok(String toolName, Object result) {
        return new ToolExecutionResponse(toolName, true, result, null, Instant.now());
    }

    public static ToolExecutionResponse fail(String toolName, String error) {
        return new ToolExecutionResponse(toolName, false, null, error, Instant.now());
    }
}
