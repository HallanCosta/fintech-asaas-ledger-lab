package com.hallancosta.ledger;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Um fato contábil publicado de uma vez. Uma transação válida sempre fecha
 * em partidas dobradas: total de débitos == total de créditos.
 */
public record LedgerTransaction(
        LedgerTransactionId id,
        Instant postedAt,
        String reference,
        List<LedgerEntry> entries
) {
    public LedgerTransaction(
            LedgerTransactionId id,
            Instant postedAt,
            String reference,
            List<LedgerEntry> entries
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.postedAt = Objects.requireNonNull(postedAt, "postedAt");
        this.reference = Objects.requireNonNull(reference, "reference");
        this.entries = List.copyOf(entries);
        validate();
    }

    public Money total(EntrySide side) {
        if (entries.isEmpty()) {
            throw new IllegalStateException("Uma transação precisa ter lançamentos");
        }

        Money total = Money.zero(entries.getFirst().amount().currency());
        for (LedgerEntry entry : entries) {
            if (entry.side() == side) {
                total = total.add(entry.amount());
            }
        }
        return total;
    }

    private void validate() {
        if (entries.size() < 2) {
            throw new IllegalArgumentException("Partidas dobradas exigem pelo menos dois lançamentos");
        }

        var transactionEntries = entries.stream()
                .filter(entry -> !entry.transactionId().equals(id))
                .toList();
        if (!transactionEntries.isEmpty()) {
            throw new IllegalArgumentException("Todo lançamento deve apontar para a própria transação");
        }

        var currency = entries.getFirst().amount().currency();
        if (entries.stream().anyMatch(entry -> !entry.amount().currency().equals(currency))) {
            throw new IllegalArgumentException("Uma transação não pode misturar moedas");
        }

        if (total(EntrySide.DEBIT).compareTo(total(EntrySide.CREDIT)) != 0) {
            throw new IllegalArgumentException("Transação desbalanceada: débitos devem ser iguais a créditos");
        }
    }
}
