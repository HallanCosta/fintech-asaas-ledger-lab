package com.hallancosta.inter;

import com.hallancosta.inter.auth.InterOAuthClient;
import com.hallancosta.inter.config.InterProperties;
import com.hallancosta.inter.external.InterBalanceResponse;
import com.hallancosta.inter.external.InterBankStatementResponse;
import com.hallancosta.inter.external.InterStatementTransaction;
import com.hallancosta.inter.model.ExternalTransactionDirection;
import com.hallancosta.inter.model.ExternalTransactionId;
import com.hallancosta.inter.model.InterTransaction;
import com.hallancosta.inter.model.Money;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.web.client.RestClient;

/** Implementação da porta de banking usando os endpoints oficiais do Inter. */
public class InterBankingClient implements InterBankingGateway {

    private static final String STATEMENT_PATH = "/banking/v2/extrato";
    private static final String BALANCE_PATH = "/banking/v2/saldo";

    private final RestClient restClient;
    private final InterOAuthClient oauthClient;
    private final InterProperties properties;

    public InterBankingClient(
            RestClient restClient,
            InterOAuthClient oauthClient,
            InterProperties properties) {
        this.restClient = Objects.requireNonNull(restClient);
        this.oauthClient = Objects.requireNonNull(oauthClient);
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    public List<InterTransaction> statement(Instant from, Instant to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("O início do extrato não pode ser depois do fim");
        }

        LocalDate start = from.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate end = to.atZone(ZoneOffset.UTC).toLocalDate();
        InterBankStatementResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(STATEMENT_PATH)
                        .queryParam("dataInicio", start)
                        .queryParam("dataFim", end)
                        .build())
                .headers(this::applyCommonHeaders)
                .retrieve()
                .body(InterBankStatementResponse.class);

        if (response == null || response.transactions() == null) {
            return Collections.emptyList();
        }

        return IntStream.range(0, response.transactions().size())
                .mapToObj(index -> toDomain(response.transactions().get(index), index))
                .toList();
    }

    public InterBalanceResponse balance() {
        return restClient.get()
                .uri(BALANCE_PATH)
                .headers(this::applyCommonHeaders)
                .retrieve()
                .body(InterBalanceResponse.class);
    }

    private InterTransaction toDomain(InterStatementTransaction source, int index) {
        BigDecimal amount = new BigDecimal(required(source.value(), "valor")).abs();
        String description = firstNonBlank(source.description(), source.title(), "Transação do Inter");
        String externalKey = String.join("|",
                String.valueOf(index),
                String.valueOf(source.entryDate()),
                source.value(),
                source.title(),
                source.description());

        return new InterTransaction(
                new ExternalTransactionId("inter-statement:" + UUID.nameUUIDFromBytes(
                        externalKey.getBytes(StandardCharsets.UTF_8))),
                accountId(),
                new Money(amount, Currency.getInstance("BRL")),
                directionOf(source.operationType()),
                parseDate(source.entryDate()),
                description);
    }

    private void applyCommonHeaders(org.springframework.http.HttpHeaders headers) {
        headers.setBearerAuth(oauthClient.accessToken());
        if (properties.getAccountId() != null && !properties.getAccountId().isBlank()) {
            headers.set("x-conta-corrente", properties.getAccountId());
        }
    }

    private String accountId() {
        return firstNonBlank(properties.getAccountId(), null, "inter-account");
    }

    private static ExternalTransactionDirection directionOf(String operationType) {
        String normalized = operationType == null
                ? ""
                : operationType.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("C") || normalized.contains("CRED")
                ? ExternalTransactionDirection.CREDIT
                : ExternalTransactionDirection.DEBIT;
    }

    private static Instant parseDate(String value) {
        String date = required(value, "dataEntrada");
        try {
            return Instant.parse(date);
        } catch (Exception ignored) {
            // O extrato pode retornar data sem horário ou uma data/hora local.
        }
        try {
            return OffsetDateTime.parse(date).toInstant();
        } catch (Exception ignored) {
            // Tenta o formato local abaixo.
        }
        try {
            return LocalDateTime.parse(date).toInstant(ZoneOffset.UTC);
        } catch (Exception ignored) {
            return LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC);
        }
    }

    private static String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O campo " + field + " veio vazio do Inter");
        }
        return value;
    }
}
