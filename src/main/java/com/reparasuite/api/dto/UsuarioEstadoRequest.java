package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotNull;

public record UsuarioEstadoRequest(
    @NotNull Boolean activo
) { }
