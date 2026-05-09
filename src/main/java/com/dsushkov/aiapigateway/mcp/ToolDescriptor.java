package com.dsushkov.aiapigateway.mcp;

import java.util.Map;

public record ToolDescriptor(
        String name,
        String description,
        Map<String, Object> inputSchema
) {
}
