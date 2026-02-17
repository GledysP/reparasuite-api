package com.reparasuite.api.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresupuestoGuardarRequest(
    @NotNull BigDecimal importe,
    @NotBlank String detalle
) { }
