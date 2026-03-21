package com.projetofmds.fraudchecker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity // Avisa ao Spring que isso é uma tabela no banco
@Table(name = "accounts") // Nome da tabela no banco
@Getter @Setter // O Lombok cria os Getters e Setters pra nós
@NoArgsConstructor // Cria construtor vazio (obrigatório para JPA)
@AllArgsConstructor // Cria construtor com todos os campos
@Builder // Ajuda a criar objetos de forma elegante

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private BigDecimal baseRiskScore; // Score de risco inicial do cliente

    // equals e hashCode são importantes para o JPA comparar objetos
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id != null && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}