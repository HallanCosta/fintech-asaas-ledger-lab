package com.hallancosta.ledger;

import java.time.Instant;

public final class LedgerStudyApplication {
    private LedgerStudyApplication() {
    }

    public static void main(String[] args) {
        var eventStore = new InMemoryEventStore();
        var eventBus = new LocalEventBus();
        var balanceProjection = new BalanceProjection();
        eventBus.subscribe(balanceProjection::on);

        var service = new PixImportService(eventStore, eventBus);
        var bankAccount = new LedgerAccountId("inter:conta-principal");
        var counterparty = new LedgerAccountId("external:cliente-1");
        var pix = new InterTransaction(
                new ExternalTransactionId("E2E-0001"),
                bankAccount,
                counterparty,
                Money.brl("100.00"),
                ExternalTransactionDirection.CREDIT,
                Instant.parse("2026-09-24T12:00:00Z"),
                "PIX recebido para estudo"
        );

        var first = service.importTransaction(pix);
        var duplicate = service.importTransaction(pix);
        var replayedProjection = new BalanceProjection();
        replayedProjection.rebuild(eventStore.loadAll());

        System.out.println("Eventos persistidos: " + eventStore.loadAll().size());
        System.out.println("Primeira importação duplicada? " + first.alreadyProcessed());
        System.out.println("Segunda importação duplicada? " + duplicate.alreadyProcessed());
        System.out.println("Saldo projetado: " + balanceProjection.balanceOf(bankAccount));
        System.out.println("Saldo após replay: " + replayedProjection.balanceOf(bankAccount));
    }
}
