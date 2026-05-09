package com.dsushkov.aiapigateway.risk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "gateway.security.api-key=postgres-it-key",
        "gateway.events.enabled=false",
        "spring.ai.mcp.server.enabled=false"
})
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_IT", matches = "true")
class RiskDecisionPostgresIntegrationTest {
    @Container
    @ServiceConnection
    @SuppressWarnings("resource") // JUnit/Testcontainers owns the static container lifecycle.
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("ai_gateway_it")
            .withUsername("ai_gateway")
            .withPassword("ai_gateway_pw");

    @LocalServerPort
    int port;

    RestClient client;

    @Autowired
    RiskDecisionRepository repository;

    @BeforeEach
    void setUpRestClient() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("X-API-Key", "postgres-it-key")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    @Test
    void scoresAndPersistsDecisionAgainstRealPostgres() {
        RiskScoreRequest request = new RiskScoreRequest(
                "cust-postgres-it",
                "tx-postgres-it",
                BigDecimal.valueOf(12500),
                "USD",
                "US",
                "WIRE_TRANSFER",
                14,
                5,
                true,
                35,
                6,
                1,
                62
        );
        ResponseEntity<RiskScoreResponse> response = client.post()
                .uri("/api/v1/risk/score")
                .body(request)
                .retrieve()
                .toEntity(RiskScoreResponse.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().decision()).isEqualTo(RiskDecision.DECLINE);
        assertThat(repository.findByTransactionId("tx-postgres-it")).isPresent();
    }
}
