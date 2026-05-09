package com.dsushkov.aiapigateway.auth;

import java.util.Map;

public record JwtPrincipal(String subject, Map<String, Object> claims) {
}
