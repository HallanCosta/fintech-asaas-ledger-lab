package com.hallancosta.ledger;

import java.util.Objects;

public record LedgerEntry(
        LedgerTransactionId transactionId,
        LedgerAccountId accountId,
        EntrySide side,
        Money amount,
        String description
) {
    public LedgerEntry {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(description, "description");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Um lançamento precisa ter valor positivo");
        }
    }
}
