package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotNull;

public record OtCambiarEstadoRequest(@NotNull String estado) { }
