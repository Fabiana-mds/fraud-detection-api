package com.projetofmds.fraudchecker.repository;

import com.projetofmds.fraudchecker.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Método extra: busca uma conta pelo número dela (ex: "12345-6")
    Optional<Account> findByAccountNumber(String accountNumber);
}