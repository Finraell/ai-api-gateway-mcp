package com.dsushkov.aiapigateway.mcp;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ToolRegistryTest {
    @Test
    void returnsFailureForUnknownTool() {
        ToolRegistry registry = new ToolRegistry(List.of(new EchoTool()), new SimpleMeterRegistry());

        ToolExecutionResponse response = registry.execute(new ToolExecutionRequest("missing.tool", Map.of()));

        assertThat(response.success()).isFalse();
        assertThat(response.error()).contains("Unknown tool");
    }

    @Test
    void executesKnownTool() {
        ToolRegistry registry = new ToolRegistry(List.of(new EchoTool()), new SimpleMeterRegistry());

        ToolExecutionResponse response = registry.execute(new ToolExecutionRequest("echo", Map.of("message", "hello")));

        assertThat(response.success()).isTrue();
        assertThat(response.result()).isEqualTo(Map.of("message", "hello"));
    }

    private static class EchoTool implements GatewayTool {
        @Override
        public ToolDescriptor descriptor() {
            return new ToolDescriptor("echo", "Echoes arguments", Map.of());
        }

        @Override
        public Object execute(Map<String, Object> arguments) {
            return arguments;
        }
    }
}
