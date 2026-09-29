package com.hallancosta.inter.pix.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PixCalendar(@JsonProperty("expiracao") Integer expiration) {
}
