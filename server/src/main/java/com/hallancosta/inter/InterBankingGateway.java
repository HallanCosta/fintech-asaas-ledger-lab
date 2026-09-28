package com.hallancosta.inter;

import com.hallancosta.inter.model.InterTransaction;

import java.time.Instant;
import java.util.List;

/**
 * Porta anticorruption: a aplicação conhece movimentações normalizadas,
 * não o JSON ou os detalhes HTTP do Inter.
 */
public interface InterBankingGateway {
    List<InterTransaction> statement(Instant from, Instant to);
}
