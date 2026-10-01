package com.hallancosta.asaas.model;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NormalizedTransactionTest {

    @Test
    void criaTransacaoNormalizada() {
        var transaction = new NormalizedTransaction(
                new ExternalTransactionId("E2E-001"),
                "conta-principal",
                Money.brl("100.00"),
                ExternalTransactionDirection.CREDIT,
                Instant.parse("2026-09-24T12:00:00Z"),
                "PIX recebido"
        );

        assertEquals("E2E-001", transaction.externalId().value());
        assertEquals(new BigDecimal("100.00"), transaction.amount().amount());
    }

    @Test
    void rejeitaValorComMaisDeDuasCasas() {
        assertThrows(ArithmeticException.class,
                () -> new Money(new BigDecimal("10.001"), java.util.Currency.getInstance("BRL")));
    }

    @Test
    void rejeitaTransacaoSemConta() {
        assertThrows(IllegalArgumentException.class, () -> new NormalizedTransaction(
                new ExternalTransactionId("E2E-002"),
                " ",
                Money.brl("1.00"),
                ExternalTransactionDirection.DEBIT,
                Instant.now(),
                "Pagamento"
        ));
    }
}
