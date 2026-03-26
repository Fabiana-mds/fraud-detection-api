package com.projetofmds.fraudchecker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projetofmds.fraudchecker.dto.RiskProfileDTO;
import com.projetofmds.fraudchecker.model.Account;
import com.projetofmds.fraudchecker.model.Transaction;
import com.projetofmds.fraudchecker.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsável pela gestão das contas dos clientes.
 */
@Service
@RequiredArgsConstructor
@Slf4j 
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public Account createAccount(Account account) {
        log.info("Salvando nova conta para o cliente: {}", account.getCostumerName());
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Calcula o perfil de risco de uma conta.
     */
    @Transactional(readOnly = true)
    public RiskProfileDTO getRiskProfile(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Conta ID {} não encontrada para perfil de risco", id);
                    return new RuntimeException("Conta não encontrada");
                });

        List<Transaction> transactions = account.getTransaction();
        int total = (transactions != null) ? transactions.size() : 0;
        
        // Cálculo da média de valores usando BigDecimal puro para evitar erros de tipo
        BigDecimal average = total == 0 ? BigDecimal.ZERO : 
            transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        // Lógica de status de risco
        String status = account.getBaseRiskScore().compareTo(new BigDecimal("100")) > 0 ? "HIGH" : "NORMAL";

        log.info("Perfil de risco gerado para conta {}: Status {}", id, status);

        return new RiskProfileDTO(
            account.getId(),
            account.getCostumerName(),
            account.getBaseRiskScore(),
            average,
            (long) total,
            status
        );
    }
}