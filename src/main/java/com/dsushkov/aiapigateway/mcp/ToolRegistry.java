package com.dsushkov.aiapigateway.mcp;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ToolRegistry {
    private final Map<String, GatewayTool> toolsByName;
    private final Counter executionsCounter;
    private final Counter failuresCounter;

    public ToolRegistry(List<GatewayTool> tools, MeterRegistry meterRegistry) {
        this.toolsByName = tools.stream()
                .collect(Collectors.toUnmodifiableMap(tool -> tool.descriptor().name(), Function.identity()));
        this.executionsCounter = Counter.builder("mcp.tools.executions.total")
                .description("Total compatibility tool executions")
                .register(meterRegistry);
        this.failuresCounter = Counter.builder("mcp.tools.failures.total")
                .description("Total compatibility tool execution failures")
                .register(meterRegistry);
    }

    public Collection<ToolDescriptor> listTools() {
        return toolsByName.values().stream()
                .map(GatewayTool::descriptor)
                .sorted(Comparator.comparing(ToolDescriptor::name))
                .toList();
    }

    public ToolExecutionResponse execute(ToolExecutionRequest request) {
        executionsCounter.increment();
        GatewayTool tool = toolsByName.get(request.toolName());
        if (tool == null) {
            failuresCounter.increment();
            return ToolExecutionResponse.fail(request.toolName(), "Unknown tool: " + request.toolName());
        }

        try {
            Object result = tool.execute(request.arguments());
            return ToolExecutionResponse.ok(request.toolName(), result);
        } catch (RuntimeException ex) {
            failuresCounter.increment();
            return ToolExecutionResponse.fail(request.toolName(), ex.getMessage());
        }
    }
}
