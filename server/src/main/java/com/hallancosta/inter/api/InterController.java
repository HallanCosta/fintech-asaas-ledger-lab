package com.hallancosta.inter.api;

import com.hallancosta.inter.InterBankingClient;
import com.hallancosta.inter.external.InterBalanceResponse;
import com.hallancosta.inter.model.InterTransaction;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Rotas de laboratório; ficam indisponíveis quando a integração está desligada. */
@RestController
@RequestMapping("/api/inter")
@ConditionalOnProperty(prefix = "inter", name = "enabled", havingValue = "true")
public class InterController {

    private final InterBankingClient bankingClient;

    public InterController(InterBankingClient bankingClient) {
        this.bankingClient = bankingClient;
    }

    @GetMapping("/balance")
    public InterBalanceResponse balance() {
        return bankingClient.balance();
    }

    @GetMapping("/statement")
    public List<InterTransaction> statement(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from não pode ser depois de to");
        }
        return bankingClient.statement(
                from.atStartOfDay().toInstant(ZoneOffset.UTC),
                to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1));
    }

}
