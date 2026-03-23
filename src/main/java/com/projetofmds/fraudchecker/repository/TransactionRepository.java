package com.projetofmds.fraudchecker.repository;

import com.projetofmds.fraudchecker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Busca todas as transações de uma conta específica
    List<Transaction> findByAccountId(Long accountId);

    // SQL Customizado para contar transações recentes
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId AND t.timestamp >= :since")
    long countTransactionsSince(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);
    
}