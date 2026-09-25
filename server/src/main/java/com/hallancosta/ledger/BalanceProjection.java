package com.hallancosta.ledger;

import java.util.HashMap;
import java.util.Map;

/**
 * Read model de saldo. Para contas de ativo, débito aumenta e crédito reduz.
 */
public final class BalanceProjection {
    private final Map<LedgerAccountId, Money> balances = new HashMap<>();

    public void on(DomainEvent event) {
        if (!(event instanceof LedgerTransactionPosted posted)) {
            return;
        }

        posted.transaction().entries().forEach(entry -> {
            var signedAmount = entry.side() == EntrySide.DEBIT
                    ? entry.amount()
                    : entry.amount().negate();
            balances.merge(entry.accountId(), signedAmount, Money::add);
        });
    }

    public Money balanceOf(LedgerAccountId accountId) {
        return balances.getOrDefault(accountId, Money.zero(java.util.Currency.getInstance("BRL")));
    }

    public void rebuild(Iterable<DomainEvent> events) {
        balances.clear();
        events.forEach(this::on);
    }
}
