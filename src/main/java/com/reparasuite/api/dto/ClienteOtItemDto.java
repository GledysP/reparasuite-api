package com.reparasuite.api.dto;

import java.time.OffsetDateTime;

public record ClienteOtItemDto(
    String codigo,
    String estado,
    String tipo,
    OffsetDateTime updatedAt
) { }
