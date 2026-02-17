package com.reparasuite.api.dto;

import jakarta.validation.constraints.AssertTrue;

public record PresupuestoAceptarRequest(
    @AssertTrue boolean acepto
) { }
