package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OtListaItemDto(
    UUID id,
    String codigo,
    String estado,
    String tipo,
    String prioridad,
    String clienteNombre,
    String tecnicoNombre,
    OffsetDateTime updatedAt
) { }
