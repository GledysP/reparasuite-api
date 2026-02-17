package com.reparasuite.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PresupuestoDto(
    UUID id,
    String estado,
    BigDecimal importe,
    String detalle,
    boolean aceptacionCheck,
    OffsetDateTime sentAt,
    OffsetDateTime respondedAt
) { }
