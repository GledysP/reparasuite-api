package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClienteResumenDto(
    UUID id,
    String nombre,
    String telefono,
    String email,

    // ✅ NUEVO: datos agregados para listado de clientes
    long totalWos,
    OffsetDateTime lastWoDate
) { }