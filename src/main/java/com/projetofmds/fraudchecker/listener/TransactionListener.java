package com.projetofmds.fraudchecker.listener;

import com.projetofmds.fraudchecker.dto.TransactionEvent;
import com.projetofmds.fraudchecker.service.TransactionService;
import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TransactionListener {

    private final TransactionService transactionService;

    @Async
    // Mudamos para TransactionalEventListener com phase AFTER_COMMIT
    @org.springframework.transaction.event.TransactionalEventListener(
        phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
    )
    public void handleTransactionCreated(TransactionEvent event) {
            try {
            transactionService.analyzeRisk(event);
        } catch (Exception e) {
            System.err.println("❌ ERRO NO ASYNC: " + e.getMessage());
            e.printStackTrace();
    }    }
}