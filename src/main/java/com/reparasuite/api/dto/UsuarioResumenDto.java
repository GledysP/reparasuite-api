package com.reparasuite.api.dto;

import java.util.UUID;

public record UsuarioResumenDto(
    UUID id,
    String nombre,
    String usuario,
    String rol,
    boolean activo
) { }
