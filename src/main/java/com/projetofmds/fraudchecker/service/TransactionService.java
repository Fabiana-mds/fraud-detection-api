package com.projetofmds.fraudchecker.service;

import com.projetofmds.fraudchecker.model.Account;
import com.projetofmds.fraudchecker.model.Transaction;
import com.projetofmds.fraudchecker.model.enums.TransactionStatus;
import com.projetofmds.fraudchecker.repository.AccountRepository;
import com.projetofmds.fraudchecker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public Transaction processTransaction(Long accountId, Transaction transaction) {
        // 1. Buscar a conta
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada!"));

        transaction.setTimestamp(LocalDateTime.now());

        // 2. Score inicial da conta
        BigDecimal accountScore = account.getBaseRiskScore();

        // 3. Regras adicionais de risco
        BigDecimal valueRisk = transaction.getAmount().divide(new BigDecimal("100"));
        BigDecimal timeRisk = BigDecimal.ZERO;
        BigDecimal frequencyRisk = BigDecimal.ZERO;

        // Regra: transações de madrugada
        int hour = LocalDateTime.now().getHour(); // Dica: use a hora de AGORA para o teste
        if (hour >= 0 && hour <= 5) {
            timeRisk = new BigDecimal("20");
        }

        // Regra: frequência (última hora)
        LocalDateTime umHoraAtras = LocalDateTime.now().minusMinutes(60);
        long recentTxCount = transactionRepository.countTransactionsSince(accountId, umHoraAtras);

        if (recentTxCount > 5) {
            long excesso = recentTxCount - 5;
            frequencyRisk = new BigDecimal(excesso).multiply(new BigDecimal("20"));
        }

        // Score final
        BigDecimal finalScore = accountScore.add(valueRisk).add(timeRisk).add(frequencyRisk);
        transaction.setBaseRiskScore(finalScore);

        // 4. Decisão
        if (finalScore.compareTo(new BigDecimal("100")) > 0 ||
            transaction.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            transaction.setStatus(TransactionStatus.REJECTED);
        } else {
            transaction.setStatus(TransactionStatus.APPROVED);
        }

        transaction.setAccount(account);
        return transactionRepository.save(transaction);
    } 

    public List<Transaction> getAccountHistory(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

} 