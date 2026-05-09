package com.dsushkov.aiapigateway.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class HmacJwtVerifier {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final boolean enabled;
    private final byte[] secret;
    private final ObjectMapper objectMapper;

    public HmacJwtVerifier(
            @Value("${gateway.security.jwt.enabled:false}") boolean enabled,
            @Value("${gateway.security.jwt.hmac-secret:local-jwt-demo-secret-change-me}") String secret,
            ObjectMapper objectMapper
    ) {
        this.enabled = enabled;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.objectMapper = objectMapper;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public JwtPrincipal verify(String token) {
        if (!enabled) {
            throw new JwtVerificationException("JWT authentication is disabled");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new JwtVerificationException("JWT must have header, payload, and signature");
        }
        String signingInput = parts[0] + "." + parts[1];
        String expectedSignature = base64Url(hmacSha256(signingInput));
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new JwtVerificationException("Invalid JWT signature");
        }
        Map<String, Object> payload = decodeJson(parts[1]);
        Object exp = payload.get("exp");
        if (exp instanceof Number number && Instant.now().getEpochSecond() >= number.longValue()) {
            throw new JwtVerificationException("JWT has expired");
        }
        String subject = payload.getOrDefault("sub", "jwt-client").toString();
        return new JwtPrincipal(subject, payload);
    }

    private Map<String, Object> decodeJson(String base64Url) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(base64Url);
            return objectMapper.readValue(decoded, MAP_TYPE);
        } catch (Exception ex) {
            throw new JwtVerificationException("Invalid JWT payload", ex);
        }
    }

    private byte[] hmacSha256(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new JwtVerificationException("Could not verify JWT signature", ex);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left.length() != right.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length(); i++) {
            result |= left.charAt(i) ^ right.charAt(i);
        }
        return result == 0;
    }
}
