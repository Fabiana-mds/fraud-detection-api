package com.projetofmds.fraudchecker.dto;

import java.math.BigDecimal;

public record TransactionEvent(
    Long transactionId,
    Long accountId,
    BigDecimal amount
) {}
