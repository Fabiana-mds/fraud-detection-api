package com.projetofmds.fraudchecker.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.projetofmds.fraudchecker.model.enums.TransactionType;
import com.projetofmds.fraudchecker.model.enums.TransactionStatus;

@Entity
@Table(name = "Transactions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING) // Mágica para salvar o texto no banco
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private BigDecimal riskScore;

    // Gatilho para preencher data/hora sozinho
    @PrePersist
    protected void  onCreate() {
        this.timestamp = LocalDateTime.now();
        if(this.status == null) {
            this.status = TransactionStatus.PENDING;
        }
    }


}
