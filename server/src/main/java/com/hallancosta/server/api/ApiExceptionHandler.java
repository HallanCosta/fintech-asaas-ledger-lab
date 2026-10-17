package com.hallancosta.server.api;

import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

/** Converte falhas externas em respostas previsíveis para o frontend. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<ApiError> handleAsaasError(RestClientResponseException exception) {
        int upstreamStatus = exception.getStatusCode().value();
        int responseStatus = switch (upstreamStatus) {
            case 400 -> 400;
            case 429 -> 503;
            case 401, 403 -> 502;
            default -> upstreamStatus >= 400 && upstreamStatus < 500 ? 400 : 502;
        };

        return ResponseEntity.status(responseStatus).body(new ApiError(
                "ASAAS_UPSTREAM_ERROR",
                "A API do Asaas rejeitou a solicitação",
                upstreamStatus,
                Instant.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleInvalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ApiError(
                "INVALID_REQUEST",
                exception.getMessage(),
                null,
                Instant.now()));
    }

    record ApiError(String code, String message, Integer upstreamStatus, Instant timestamp) {
    }
}
