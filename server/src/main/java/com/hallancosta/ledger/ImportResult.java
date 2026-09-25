package com.hallancosta.ledger;

public record ImportResult(LedgerTransactionId transactionId, boolean alreadyProcessed) {
}
