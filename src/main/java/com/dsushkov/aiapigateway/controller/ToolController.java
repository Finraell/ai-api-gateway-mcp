package com.dsushkov.aiapigateway.controller;

import com.dsushkov.aiapigateway.mcp.ToolDescriptor;
import com.dsushkov.aiapigateway.mcp.ToolExecutionRequest;
import com.dsushkov.aiapigateway.mcp.ToolExecutionResponse;
import com.dsushkov.aiapigateway.mcp.ToolRegistry;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/tools")
public class ToolController {
    private final ToolRegistry toolRegistry;

    public ToolController(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @Operation(summary = "List Spring AI MCP tools exposed by the gateway")
    @GetMapping
    public ResponseEntity<Collection<ToolDescriptor>> listTools() {
        return ResponseEntity.ok(toolRegistry.listTools());
    }

    @Operation(summary = "Execute a tool by name")
    @PostMapping("/execute")
    public ResponseEntity<ToolExecutionResponse> execute(@Valid @RequestBody ToolExecutionRequest request) {
        ToolExecutionResponse response = toolRegistry.execute(request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}
