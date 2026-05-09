package com.dsushkov.aiapigateway.controller;

import com.dsushkov.aiapigateway.idempotency.IdempotencyService;
import com.dsushkov.aiapigateway.risk.RiskDecisionService;
import com.dsushkov.aiapigateway.risk.RiskScoreRequest;
import com.dsushkov.aiapigateway.risk.RiskScoreResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/risk")
public class RiskDecisionController {
    private final RiskDecisionService riskDecisionService;
    private final IdempotencyService idempotencyService;

    public RiskDecisionController(RiskDecisionService riskDecisionService, IdempotencyService idempotencyService) {
        this.riskDecisionService = riskDecisionService;
        this.idempotencyService = idempotencyService;
    }

    @Operation(summary = "Score a financial transaction and persist the risk decision")
    @PostMapping("/score")
    public ResponseEntity<RiskScoreResponse> score(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody RiskScoreRequest request
    ) {
        RiskScoreResponse response = idempotencyService.execute(idempotencyKey, () -> riskDecisionService.evaluate(request));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get a risk decision by decision id")
    @GetMapping("/decisions/{decisionId}")
    public ResponseEntity<RiskScoreResponse> getDecision(@PathVariable String decisionId) {
        return ResponseEntity.ok(riskDecisionService.findByDecisionId(decisionId));
    }

    @Operation(summary = "Get recent decisions for a customer")
    @GetMapping("/customers/{customerId}/decisions")
    public ResponseEntity<List<RiskScoreResponse>> getCustomerDecisions(@PathVariable String customerId) {
        return ResponseEntity.ok(riskDecisionService.findRecentByCustomerId(customerId));
    }
}
