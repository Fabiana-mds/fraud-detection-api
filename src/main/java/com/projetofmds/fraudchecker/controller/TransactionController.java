package com.projetofmds.fraudchecker.controller;

import com.projetofmds.fraudchecker.dto.TransactionRequest;
import com.projetofmds.fraudchecker.dto.TransactionResponse;
import com.projetofmds.fraudchecker.model.Transaction;
import com.projetofmds.fraudchecker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions") // Simplificamos a rota principal
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody @Valid TransactionRequest request) {
        
        // 1. Chamamos o service passando o DTO (isso vai dar erro no Service por enquanto)
        Transaction transaction = transactionService.processTransaction(request);
        
        // 2. Convertemos a Entidade para o Record de Resposta (Mapeamento manual)
        TransactionResponse response = new TransactionResponse(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getTimestamp(),
            transaction.getStatus().name(),
            transaction.getBaseRiskScore()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/account/{accountId}") 
    public ResponseEntity<List<Transaction>> getHistory(@PathVariable Long accountId) {
        List<Transaction> history = transactionService.getAccountHistory(accountId);
        return ResponseEntity.ok(history);
    }
}