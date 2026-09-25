package com.hallancosta.ledger;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerLearningTest {

    @Test
    void umaTransacaoDesbalanceadaNaoPodeSerPublicada() {
        var id = LedgerTransactionId.newId();
        var account = new LedgerAccountId("inter:conta");
        var counterparty = new LedgerAccountId("external:cliente");

        assertThrows(IllegalArgumentException.class, () -> new LedgerTransaction(
                id,
                Instant.now(),
                "desbalanceada",
                List.of(
                        new LedgerEntry(id, account, EntrySide.DEBIT, Money.brl("10.00"), "débito"),
                        new LedgerEntry(id, counterparty, EntrySide.CREDIT, Money.brl("9.00"), "crédito")
                )
        ));
    }

    @Test
    void webhookDuplicadoGeraUmEfeitoFinanceiro() {
        var eventStore = new InMemoryEventStore();
        var eventBus = new LocalEventBus();
        var service = new PixImportService(eventStore, eventBus);
        var external = incomingPix("E2E-DUPLICADO");

        var first = service.importTransaction(external);
        var duplicate = service.importTransaction(external);

        assertFalse(first.alreadyProcessed());
        assertTrue(duplicate.alreadyProcessed());
        assertEquals(first.transactionId(), duplicate.transactionId());
        assertEquals(2, eventStore.loadAll().size());
    }

    @Test
    void projectionPodeSerDestruidaEReconstruidaPorReplay() {
        var eventStore = new InMemoryEventStore();
        var eventBus = new LocalEventBus();
        var liveProjection = new BalanceProjection();
        eventBus.subscribe(liveProjection::on);
        var service = new PixImportService(eventStore, eventBus);
        var external = incomingPix("E2E-REPLAY");

        service.importTransaction(external);

        var replayedProjection = new BalanceProjection();
        replayedProjection.rebuild(eventStore.loadAll());

        assertEquals(liveProjection.balanceOf(external.bankAccountId()),
                replayedProjection.balanceOf(external.bankAccountId()));
        assertEquals(Money.brl("100.00"), replayedProjection.balanceOf(external.bankAccountId()));
    }

    @Test
    void eventStoreProtegeAOrdemDoStreamComConcorrenciaOtimista() {
        var eventStore = new InMemoryEventStore();
        var event = new PixReceived(
                UUID.randomUUID(),
                "account:inter:conta",
                new ExternalTransactionId("E2E-CONCORRENCIA"),
                Money.brl("1.00"),
                Instant.now(),
                "teste"
        );

        eventStore.append("account:inter:conta", List.of(event), 0);

        assertThrows(InMemoryEventStore.OptimisticConcurrencyException.class,
                () -> eventStore.append("account:inter:conta", List.of(event), 0));
    }

    private InterTransaction incomingPix(String externalId) {
        return new InterTransaction(
                new ExternalTransactionId(externalId),
                new LedgerAccountId("inter:conta-principal"),
                new LedgerAccountId("external:cliente-1"),
                Money.brl("100.00"),
                ExternalTransactionDirection.CREDIT,
                Instant.parse("2026-09-24T12:00:00Z"),
                "PIX recebido para teste"
        );
    }
}
