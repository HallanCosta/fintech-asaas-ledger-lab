package com.hallancosta.inter.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Valor monetário sem ponto flutuante. A primeira versão trabalha somente com BRL.
 */
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Money brl(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("BRL"));
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    @Override
    public int compareTo(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Moedas diferentes não podem ser comparadas");
        }
        return amount.compareTo(other.amount);
    }
}
