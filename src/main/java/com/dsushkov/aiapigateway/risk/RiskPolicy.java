package com.dsushkov.aiapigateway.risk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RiskPolicy {
    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("KP", "IR", "SY");
    private static final Set<String> ELEVATED_RISK_COUNTRIES = Set.of("RU", "BY", "VE");
    private static final Set<String> HIGH_RISK_CATEGORIES = Set.of("CRYPTO", "WIRE_TRANSFER", "GIFT_CARD", "MONEY_TRANSFER");

    private final String modelVersion;

    public RiskPolicy(@Value("${gateway.risk.model-version:rules-local}") String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String modelVersion() {
        return modelVersion;
    }

    public boolean highRiskCountry(String countryCode) {
        return HIGH_RISK_COUNTRIES.contains(countryCode);
    }

    public boolean elevatedRiskCountry(String countryCode) {
        return ELEVATED_RISK_COUNTRIES.contains(countryCode);
    }

    public boolean highRiskMerchantCategory(String category) {
        return HIGH_RISK_CATEGORIES.contains(category);
    }

    public int reviewThreshold() {
        return 35;
    }

    public int declineThreshold() {
        return 70;
    }

    public Set<String> highRiskCategories() {
        return HIGH_RISK_CATEGORIES;
    }
}
