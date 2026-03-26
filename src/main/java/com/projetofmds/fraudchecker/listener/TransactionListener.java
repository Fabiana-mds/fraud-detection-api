package com.projetofmds.fraudchecker.listener;

import com.projetofmds.fraudchecker.dto.TransactionEventDTO;
import com.projetofmds.fraudchecker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionListener {

    private final TransactionService transactionService;

    @Async
    // Mudamos para TransactionalEventListener com phase AFTER_COMMIT
    @org.springframework.transaction.event.TransactionalEventListener(
        phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
    )
    public void handleTransactionCreated(TransactionEventDTO event) {
            try {
            transactionService.analyzeRisk(event);
        } catch (Exception e) {
            log.error("❌ ERRO NO ASYNC: " + e.getMessage());
            e.printStackTrace();
    }    }
}