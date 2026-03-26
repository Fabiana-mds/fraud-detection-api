package com.projetofmds.fraudchecker.service;

import com.projetofmds.fraudchecker.model.Account;
import com.projetofmds.fraudchecker.model.Transaction;
import com.projetofmds.fraudchecker.model.enums.TransactionStatus;
import com.projetofmds.fraudchecker.model.enums.TransactionType;
import com.projetofmds.fraudchecker.repository.AccountRepository;
import com.projetofmds.fraudchecker.repository.TransactionRepository;
import com.projetofmds.fraudchecker.strategy.RiskRuleChecker;
import com.projetofmds.fraudchecker.dto.TransactionEventDTO;
import com.projetofmds.fraudchecker.dto.TransactionRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // [cite: 218] Ativa o objeto 'log' automaticamente via Lombok
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final List<RiskRuleChecker> riskRules;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Transaction processTransaction(TransactionRequestDTO request) {
        log.info("Recebendo nova transação para a conta: {}", request.accountId()); // 

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> {
                    log.error("Falha ao processar: Conta {} não encontrada!", request.accountId());
                    return new RuntimeException("Conta não encontrada!");
                });

        Transaction transaction = Transaction.builder()
            .amount(request.amount())
            .type(TransactionType.valueOf(request.typeString()))
            .account(account)
            .timestamp(LocalDateTime.now())
            .status(TransactionStatus.PENDING)
            .baseRiskScore(BigDecimal.ZERO)
            .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transação {} salva com status PENDING. Disparando evento assíncrono...", saved.getId());

        eventPublisher.publishEvent(new TransactionEventDTO(
            saved.getId(), 
            account.getId(), 
            saved.getAmount()
        ));

        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void analyzeRisk(TransactionEventDTO event) {
        //cite:316,317
        log.info("🔍 [ASSÍNCRONO] Iniciando análise de risco para Transação ID: {}", event.transactionId());

        Transaction transaction = transactionRepository.findById(event.transactionId())
                .orElseThrow(() -> {
                    log.error("Erro crítico: Transação {} sumiu do banco!", event.transactionId());
                    return new RuntimeException("Transação não encontrada no processamento posterior");
                });
        
        Account account = transaction.getAccount();
        BigDecimal totalRisk = account.getBaseRiskScore();

        // --- STRATEGY PATTERN ---
        for (RiskRuleChecker rule : riskRules) {
            BigDecimal ruleScore = rule.check(transaction);
            totalRisk = totalRisk.add(ruleScore);
        }
        
        transaction.setBaseRiskScore(totalRisk);

        // Decisão final baseada no score ACUMULADO ou valor exorbitante [cite: 301, 302]
        if (totalRisk.compareTo(new BigDecimal("100")) > 0 || 
            transaction.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            transaction.setStatus(TransactionStatus.REJECTED);
            log.warn("🚨 Transação {} REJEITADA. Score: {} | Valor: {}", 
                     transaction.getId(), totalRisk, transaction.getAmount());
        } else {
            transaction.setStatus(TransactionStatus.APPROVED);
            log.info("✅ Transação {} APROVADA. Score: {}", transaction.getId(), totalRisk);
        }

        // --- APRENDIZADO ---
        account.setBaseRiskScore(totalRisk);
        accountRepository.save(account); 
        
        transactionRepository.save(transaction);
        //cite: 314,315
        log.info("📈 [APRENDIZADO] Novo Score da Conta {}: {}", account.getId(), totalRisk);
    }

    public List<Transaction> getAccountHistory(Long accountId) {
        log.debug("Buscando histórico para conta: {}", accountId);
        return transactionRepository.findByAccountId(accountId);
    }
}