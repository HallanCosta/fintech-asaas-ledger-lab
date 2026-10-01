package com.hallancosta.asaas;

import com.hallancosta.asaas.config.AsaasProperties;
import com.hallancosta.asaas.external.AsaasBalanceResponse;
import com.hallancosta.asaas.external.AsaasPixQrCodeResponse;
import com.hallancosta.asaas.external.AsaasStatementResponse;
import com.hallancosta.asaas.external.AsaasStatementTransaction;
import com.hallancosta.asaas.external.AsaasTransferResponse;
import com.hallancosta.asaas.model.ExternalTransactionDirection;
import com.hallancosta.asaas.model.ExternalTransactionId;
import com.hallancosta.asaas.model.Money;
import com.hallancosta.asaas.model.NormalizedTransaction;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

/** Adapter didático para os endpoints financeiros da API Asaas. */
public class AsaasClient implements AsaasGateway {

    private static final String STATEMENT_PATH = "/financialTransactions";
    private static final String BALANCE_PATH = "/finance/balance";

    private final RestClient restClient;
    private final AsaasProperties properties;

    public AsaasClient(RestClient restClient, AsaasProperties properties) {
        this.restClient = Objects.requireNonNull(restClient);
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    public List<NormalizedTransaction> statement(Instant from, Instant to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("O início do extrato não pode ser depois do fim");
        }

        LocalDate start = from.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate end = to.atZone(ZoneOffset.UTC).toLocalDate();
        AsaasStatementResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(STATEMENT_PATH)
                        .queryParam("startDate", start)
                        .queryParam("finishDate", end)
                        .queryParam("limit", 100)
                        .queryParam("offset", 0)
                        .build())
                .headers(this::applyCommonHeaders)
                .retrieve()
                .body(AsaasStatementResponse.class);

        if (response == null || response.transactions() == null) {
            return Collections.emptyList();
        }

        return IntStream.range(0, response.transactions().size())
                .mapToObj(index -> toDomain(response.transactions().get(index), index))
                .toList();
    }

    public AsaasBalanceResponse balance() {
        return restClient.get()
                .uri(BALANCE_PATH)
                .headers(this::applyCommonHeaders)
                .retrieve()
                .body(AsaasBalanceResponse.class);
    }

    public Map<String, Object> createPixPayment(
            String customer,
            BigDecimal value,
            LocalDate dueDate,
            String description,
            String externalReference) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customer", required(customer, "customer"));
        body.put("billingType", "PIX");
        body.put("value", Objects.requireNonNull(value, "value"));
        body.put("dueDate", Objects.requireNonNull(dueDate, "dueDate"));
        if (description != null && !description.isBlank()) {
            body.put("description", description);
        }
        if (externalReference != null && !externalReference.isBlank()) {
            body.put("externalReference", externalReference);
        }

        Map<String, Object> response = restClient.post()
                .uri("/payments")
                .headers(this::applyCommonHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return response == null ? Map.of() : response;
    }

    public AsaasPixQrCodeResponse pixQrCode(String paymentId) {
        return restClient.get()
                .uri("/payments/{id}/pixQrCode", required(paymentId, "paymentId"))
                .headers(this::applyCommonHeaders)
                .retrieve()
                .body(AsaasPixQrCodeResponse.class);
    }

    public AsaasTransferResponse transferPix(
            BigDecimal value,
            String pixAddressKey,
            String pixAddressKeyType,
            String description,
            String externalReference,
            LocalDate scheduleDate) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("value", Objects.requireNonNull(value, "value"));
        body.put("operationType", "PIX");
        body.put("pixAddressKey", required(pixAddressKey, "pixAddressKey"));
        body.put("pixAddressKeyType", required(pixAddressKeyType, "pixAddressKeyType").toUpperCase(Locale.ROOT));
        if (description != null && !description.isBlank()) {
            body.put("description", description);
        }
        if (externalReference != null && !externalReference.isBlank()) {
            body.put("externalReference", externalReference);
        }
        if (scheduleDate != null) {
            body.put("scheduleDate", scheduleDate);
        }

        return restClient.post()
                .uri("/transfers")
                .headers(this::applyCommonHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(AsaasTransferResponse.class);
    }

    private NormalizedTransaction toDomain(AsaasStatementTransaction source, int index) {
        BigDecimal rawValue = Objects.requireNonNull(source.value(), "value");
        BigDecimal amount = rawValue.abs();
        String description = firstNonBlank(source.description(), source.type(), "Movimentação Asaas");
        String externalKey = String.join("|",
                String.valueOf(index),
                String.valueOf(source.id()),
                String.valueOf(source.date()),
                rawValue.toPlainString(),
                description);

        return new NormalizedTransaction(
                new ExternalTransactionId("asaas-statement:" + UUID.nameUUIDFromBytes(
                        externalKey.getBytes(StandardCharsets.UTF_8))),
                accountId(),
                new Money(amount, Currency.getInstance("BRL")),
                rawValue.signum() >= 0
                        ? ExternalTransactionDirection.CREDIT
                        : ExternalTransactionDirection.DEBIT,
                parseDate(source.date()),
                description);
    }

    private void applyCommonHeaders(HttpHeaders headers) {
        headers.set("access_token", required(properties.getApiKey(), "asaas.api-key"));
        headers.set(HttpHeaders.USER_AGENT, "fintech-pix-lab/0.1");
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    }

    private String accountId() {
        return firstNonBlank(properties.getAccountId(), null, "asaas-account");
    }

    private static Instant parseDate(String value) {
        String date = required(value, "date");
        try {
            return Instant.parse(date);
        } catch (Exception ignored) {
            // O extrato normalmente retorna uma data, mas aceitamos data/hora para o estudo.
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
            throw new IllegalArgumentException("O campo " + field + " é obrigatório");
        }
        return value;
    }
}
