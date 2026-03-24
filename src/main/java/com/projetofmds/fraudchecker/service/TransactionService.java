package com.projetofmds.fraudchecker.service;

import com.projetofmds.fraudchecker.model.Account;
import com.projetofmds.fraudchecker.model.Transaction;
import com.projetofmds.fraudchecker.model.enums.TransactionStatus;
import com.projetofmds.fraudchecker.model.enums.TransactionType;
import com.projetofmds.fraudchecker.repository.AccountRepository;
import com.projetofmds.fraudchecker.repository.TransactionRepository;
import com.projetofmds.fraudchecker.strategy.RiskRuleChecker;
import com.projetofmds.fraudchecker.dto.TransactionEvent;
import com.projetofmds.fraudchecker.dto.TransactionRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher; // IMPORTANTE
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final List<RiskRuleChecker> riskRules;
    private final ApplicationEventPublisher eventPublisher; // O "estilingue" de eventos

    // 1. MÉTODO RÁPIDO: Salva como PENDING e avisa o sistema
    @Transactional
    public Transaction processTransaction(TransactionRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new RuntimeException("Conta não encontrada!"));

        Transaction transaction = Transaction.builder()
            .amount(request.amount())
            .type(TransactionType.valueOf(request.typeString()))
            .account(account)
            .timestamp(LocalDateTime.now())
            .status(TransactionStatus.PENDING) // Começa Pendente
            .baseRiskScore(BigDecimal.ZERO)
            .build();

        // Salva rápido no banco
        Transaction saved = transactionRepository.save(transaction);

        // Dispara o evento para o Listener trabalhar em background
        eventPublisher.publishEvent(new TransactionEvent(
            saved.getId(), 
            account.getId(), 
            saved.getAmount()
        ));

        return saved;
    }

    // 2. MÉTODO ASSÍNCRONO: Chamado pelo Listener para calcular o risco
   @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void analyzeRisk(TransactionEvent event) {
        // Busca a transação que acabou de ser salva
        Transaction transaction = transactionRepository.findById(event.transactionId())
                .orElseThrow(() -> new RuntimeException("Transação não encontrada no processamento posterior"));
        
        Account account = transaction.getAccount();

        // --- STRATEGY PATTERN ---
        BigDecimal totalRisk = account.getBaseRiskScore();

        for (RiskRuleChecker rule : riskRules) {
            totalRisk = totalRisk.add(rule.check(transaction));
        }
        
        transaction.setBaseRiskScore(totalRisk);

        // Decisão final baseada no score calculado
        if (totalRisk.compareTo(new BigDecimal("100")) > 0 || 
            transaction.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            transaction.setStatus(TransactionStatus.REJECTED);
        } else {
            transaction.setStatus(TransactionStatus.APPROVED);
        }

        // Atualiza a transação no banco com o novo Status e Score
        transactionRepository.save(transaction);
        
        System.out.println("⚡ [ASSÍNCRONO] Transação " + transaction.getId() + " finalizada como: " + transaction.getStatus());
    }

    public List<Transaction> getAccountHistory(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }
}