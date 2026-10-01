package com.hallancosta.asaas.model;

import java.time.Instant;
import java.util.Objects;

/** Modelo normalizado; o domínio não depende do JSON específico do provedor. */
public record NormalizedTransaction(
        ExternalTransactionId externalId,
        String accountId,
        Money amount,
        ExternalTransactionDirection direction,
        Instant occurredAt,
        String description
) {
    public NormalizedTransaction {
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(description, "description");
        if (accountId.isBlank()) {
            throw new IllegalArgumentException("A conta não pode ser vazia");
        }
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Movimentação externa precisa ter valor positivo");
        }
    }
}
