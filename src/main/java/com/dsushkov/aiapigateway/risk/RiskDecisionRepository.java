package com.dsushkov.aiapigateway.risk;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiskDecisionRepository extends JpaRepository<RiskDecisionEntity, String> {
    Optional<RiskDecisionEntity> findByTransactionId(String transactionId);
    List<RiskDecisionEntity> findTop20ByCustomerIdOrderByCreatedAtDesc(String customerId);
}
