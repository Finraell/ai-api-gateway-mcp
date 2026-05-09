package com.dsushkov.aiapigateway.risk;

public class DecisionNotFoundException extends RuntimeException {
    public DecisionNotFoundException(String id) {
        super("Risk decision not found: " + id);
    }
}
