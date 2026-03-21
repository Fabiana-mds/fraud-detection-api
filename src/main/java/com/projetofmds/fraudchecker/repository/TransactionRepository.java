package com.projetofmds.fraudchecker.repository;

import com.projetofmds.fraudchecker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Por enquanto, os métodos padrão do JpaRepository (save, findById, delete) são suficientes
}