package com.dsushkov.aiapigateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthenticationFilterTest {
    private final TestableApiKeyAuthenticationFilter filter = new TestableApiKeyAuthenticationFilter("test-key");

    @Test
    void skipsPrometheusScrapeEndpoint() {
        assertThat(filter.shouldSkip("GET", "/actuator/prometheus")).isTrue();
    }

    @Test
    void stillProtectsBusinessEndpoints() {
        assertThat(filter.shouldSkip("POST", "/api/v1/risk/score")).isFalse();
    }

    private static class TestableApiKeyAuthenticationFilter extends ApiKeyAuthenticationFilter {
        TestableApiKeyAuthenticationFilter(String expectedApiKey) {
            super(expectedApiKey, true);
        }

        boolean shouldSkip(String method, String uri) {
            MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
            request.setRequestURI(uri);
            return shouldNotFilter(request);
        }
    }
}
