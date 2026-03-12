package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InventarioMovimientoCrearRequest(
    @NotBlank String tipoMovimiento,
    @NotBlank String cantidad,
    String costoUnitario,
    @Size(max = 200) String motivo,
    @Size(max = 4000) String observacion
) {}