package com.dsushkov.aiapigateway.mcp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ToolExecutionRequest(
        @NotBlank String toolName,
        @NotNull Map<String, Object> arguments
) {
}
