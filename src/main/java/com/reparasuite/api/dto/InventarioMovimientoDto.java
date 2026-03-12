package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InventarioMovimientoDto(
    UUID id,
    String tipoMovimiento,
    String cantidad,
    String stockAnterior,
    String stockResultante,
    String costoUnitario,
    String motivo,
    String observacion,
    OffsetDateTime fechaMovimiento
) {}