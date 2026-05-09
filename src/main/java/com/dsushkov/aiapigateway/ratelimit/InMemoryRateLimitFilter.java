package com.dsushkov.aiapigateway.ratelimit;

import com.dsushkov.aiapigateway.observability.RiskMetrics;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryRateLimitFilter extends OncePerRequestFilter {
    private final boolean enabled;
    private final int requestsPerMinute;
    private final RiskMetrics riskMetrics;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public InMemoryRateLimitFilter(
            @Value("${gateway.rate-limit.enabled:true}") boolean enabled,
            @Value("${gateway.rate-limit.requests-per-minute:120}") int requestsPerMinute,
            RiskMetrics riskMetrics
    ) {
        this.enabled = enabled;
        this.requestsPerMinute = Math.max(1, requestsPerMinute);
        this.riskMetrics = riskMetrics;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !enabled
                || HttpMethod.OPTIONS.matches(request.getMethod())
                || path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = clientKey(request);
        Window window = windows.compute(key, (ignored, existing) -> {
            Instant now = Instant.now();
            if (existing == null || Duration.between(existing.startedAt(), now).toMinutes() >= 1) {
                return new Window(now, new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });

        int used = window.count().get();
        int remaining = Math.max(0, requestsPerMinute - used);
        response.setHeader("X-RateLimit-Limit", Integer.toString(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", Integer.toString(remaining));

        if (used > requestsPerMinute) {
            riskMetrics.recordRateLimitDecision(request.getRequestURI(), false);
            response.setStatus(429);
            response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"limitPerMinute\":" + requestsPerMinute + "}");
            return;
        }

        riskMetrics.recordRateLimitDecision(request.getRequestURI(), true);
        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String remote = forwardedFor == null || forwardedFor.isBlank()
                ? request.getRemoteAddr()
                : forwardedFor.split(",")[0].trim();
        return remote + ":" + request.getRequestURI();
    }

    private record Window(Instant startedAt, AtomicInteger count) {
    }
}
