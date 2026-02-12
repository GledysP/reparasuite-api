package com.reparasuite.api.dto;

import java.time.OffsetDateTime;

public record OtClienteListaItemDto(
    String codigo,
    String estado,
    String tipo,
    OffsetDateTime updatedAt
) { }
