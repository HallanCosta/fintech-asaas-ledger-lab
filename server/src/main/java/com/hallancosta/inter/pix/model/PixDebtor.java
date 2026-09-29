package com.hallancosta.inter.pix.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PixDebtor(
        String cpf,
        String cnpj,
        @JsonProperty("nome") String name) {
}
