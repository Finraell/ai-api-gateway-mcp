package com.dsushkov.aiapigateway.risk;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record RiskScoreRequest(
        @NotBlank String customerId,
        @NotBlank String transactionId,
        @NotNull @DecimalMin("0.01") BigDecimal transactionAmount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotBlank @Pattern(regexp = "^[A-Z]{2}$") String countryCode,
        @NotBlank String merchantCategory,
        @Min(0) int accountAgeDays,
        @Min(0) int failedLoginCount,
        boolean newDevice,
        @Min(0) @Max(100) int deviceTrustScore,
        @Min(0) int velocity30m,
        @Min(0) int previousChargebacks,
        @Min(0) @Max(100) int ipRiskScore
) {
}
