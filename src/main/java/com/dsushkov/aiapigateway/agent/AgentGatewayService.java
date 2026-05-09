package com.dsushkov.aiapigateway.agent;

import com.dsushkov.aiapigateway.mcp.ToolExecutionRequest;
import com.dsushkov.aiapigateway.mcp.ToolExecutionResponse;
import com.dsushkov.aiapigateway.mcp.ToolRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AgentGatewayService {
    private final ToolRegistry toolRegistry;

    public AgentGatewayService(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public AgentDecisionResponse answer(AgentDecisionRequest request) {
        String prompt = request.userPrompt().toLowerCase(Locale.ROOT);
        Map<String, Object> context = request.context() == null ? Map.of() : request.context();
        List<ToolExecutionResponse> results = new ArrayList<>();

        if (context.containsKey("transaction")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> transaction = (Map<String, Object>) context.get("transaction");
            results.add(toolRegistry.execute(new ToolExecutionRequest("risk.score.transaction", transaction)));
        }

        if (prompt.contains("policy") || prompt.contains("threshold")) {
            results.add(toolRegistry.execute(new ToolExecutionRequest("risk.explain.policy", Map.of("topic", "thresholds"))));
        }

        if (context.containsKey("merchantCategory")) {
            results.add(toolRegistry.execute(new ToolExecutionRequest("merchant.risk.lookup", Map.of("merchantCategory", context.get("merchantCategory")))));
        }

        if (results.isEmpty()) {
            results.add(toolRegistry.execute(new ToolExecutionRequest("risk.explain.policy", Map.of("topic", "overview"))));
        }

        String finalAnswer = results.stream().anyMatch(ToolExecutionResponse::success)
                ? "Completed the requested analysis using the gateway tool registry. Review toolResults for the auditable output."
                : "The gateway could not complete the analysis. Review toolResults for validation errors.";

        return new AgentDecisionResponse("Select the smallest set of auditable tools, execute them, and return explainable results.", List.copyOf(results), finalAnswer);
    }
}
