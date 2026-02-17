package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotNull;

public record UsuarioEstadoRequest(@NotNull boolean activo) { }
