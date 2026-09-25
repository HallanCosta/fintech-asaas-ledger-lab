package com.hallancosta.ledger;

import java.time.Instant;
import java.util.Objects;

/**
 * Modelo normalizado. O domínio não deve depender do JSON específico do Inter.
 */
public record InterTransaction(
        ExternalTransactionId externalId,
        LedgerAccountId bankAccountId,
        LedgerAccountId counterpartyAccountId,
        Money amount,
        ExternalTransactionDirection direction,
        Instant occurredAt,
        String description
) {
    public InterTransaction {
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(bankAccountId, "bankAccountId");
        Objects.requireNonNull(counterpartyAccountId, "counterpartyAccountId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(description, "description");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Movimentação externa precisa ter valor positivo");
        }
    }
}
