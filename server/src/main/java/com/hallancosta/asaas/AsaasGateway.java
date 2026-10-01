package com.hallancosta.asaas;

import com.hallancosta.asaas.model.NormalizedTransaction;
import java.time.Instant;
import java.util.List;

/** Porta anticorruption: o restante da aplicação não conhece o JSON do Asaas. */
public interface AsaasGateway {
    List<NormalizedTransaction> statement(Instant from, Instant to);
}
