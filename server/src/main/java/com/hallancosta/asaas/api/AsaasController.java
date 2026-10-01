package com.hallancosta.asaas.api;

import com.hallancosta.asaas.AsaasClient;
import com.hallancosta.asaas.external.AsaasBalanceResponse;
import com.hallancosta.asaas.external.AsaasPixQrCodeResponse;
import com.hallancosta.asaas.external.AsaasTransferResponse;
import com.hallancosta.asaas.model.NormalizedTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Rotas de laboratório; ficam indisponíveis quando a integração está desligada. */
@RestController
@RequestMapping("/api/asaas")
@ConditionalOnProperty(prefix = "asaas", name = "enabled", havingValue = "true")
public class AsaasController {

    private final AsaasClient client;

    public AsaasController(AsaasClient client) {
        this.client = client;
    }

    @GetMapping("/balance")
    public AsaasBalanceResponse balance() {
        return client.balance();
    }

    @GetMapping("/statement")
    public List<NormalizedTransaction> statement(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from não pode ser depois de to");
        }
        return client.statement(
                from.atStartOfDay().toInstant(ZoneOffset.UTC),
                to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1));
    }

    @PostMapping("/payments/pix")
    public Map<String, Object> createPixPayment(@RequestBody PixPaymentRequest request) {
        return client.createPixPayment(
                request.customer(),
                request.value(),
                request.dueDate(),
                request.description(),
                request.externalReference());
    }

    @GetMapping("/payments/{paymentId}/pix-qrcode")
    public AsaasPixQrCodeResponse pixQrCode(@PathVariable String paymentId) {
        return client.pixQrCode(paymentId);
    }

    @PostMapping("/transfers/pix")
    public AsaasTransferResponse transferPix(@RequestBody PixTransferRequest request) {
        return client.transferPix(
                request.value(),
                request.pixAddressKey(),
                request.pixAddressKeyType(),
                request.description(),
                request.externalReference(),
                request.scheduleDate());
    }

    public record PixPaymentRequest(
            String customer,
            BigDecimal value,
            LocalDate dueDate,
            String description,
            String externalReference) {
    }

    public record PixTransferRequest(
            BigDecimal value,
            String pixAddressKey,
            String pixAddressKeyType,
            String description,
            String externalReference,
            LocalDate scheduleDate) {
    }
}
