package com.projetofmds.fraudchecker.dto;

import java.math.BigDecimal;


public record RiskProfileDTO(
    Long accountId,
    String customerName,
    BigDecimal currentRiskScore,
    BigDecimal averageTransactionValue,
    Long totalTransactions,
    String riskStatus // Ex: "LOW", "MEDIUM", "HIGH"
) {}