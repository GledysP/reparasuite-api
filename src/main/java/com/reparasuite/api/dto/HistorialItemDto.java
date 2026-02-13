package com.reparasuite.api.dto;

import java.time.OffsetDateTime;

public record HistorialItemDto(
    OffsetDateTime fecha,
    String evento,
    String descripcion,
    HistorialUsuarioDto usuario
) { }
