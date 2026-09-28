package com.hallancosta.inter.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Modelo normalizado. O domínio não depende do JSON específico do Inter.
 */
public record InterTransaction(
        ExternalTransactionId externalId,
        String accountId,
        Money amount,
        ExternalTransactionDirection direction,
        Instant occurredAt,
        String description
) {
    public InterTransaction {
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(description, "description");
        if (accountId.isBlank()) {
            throw new IllegalArgumentException("A conta do Inter não pode ser vazia");
        }
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Movimentação externa precisa ter valor positivo");
        }
    }
}
