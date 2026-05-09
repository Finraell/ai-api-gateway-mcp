package com.dsushkov.aiapigateway.mcp;

import java.util.Map;

public interface GatewayTool {
    ToolDescriptor descriptor();
    Object execute(Map<String, Object> arguments);
}
