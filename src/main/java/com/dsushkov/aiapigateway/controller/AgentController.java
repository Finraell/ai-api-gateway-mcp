package com.dsushkov.aiapigateway.controller;

import com.dsushkov.aiapigateway.agent.AgentDecisionRequest;
import com.dsushkov.aiapigateway.agent.AgentDecisionResponse;
import com.dsushkov.aiapigateway.agent.AgentGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {
    private final AgentGatewayService agentGatewayService;

    public AgentController(AgentGatewayService agentGatewayService) {
        this.agentGatewayService = agentGatewayService;
    }

    @Operation(summary = "Demo agent facade that selects and executes Spring AI MCP tools")
    @PostMapping("/decisions")
    public ResponseEntity<AgentDecisionResponse> decide(@Valid @RequestBody AgentDecisionRequest request) {
        return ResponseEntity.ok(agentGatewayService.answer(request));
    }
}
