package com.hallancosta.inter.pix.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImmediatePixChargeRequest(
        String txid,
        PixCalendar calendario,
        PixDebtor devedor,
        PixValue valor,
        String chave,
        String solicitacaoPagador) {
}
