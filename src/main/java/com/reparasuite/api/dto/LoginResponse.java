package com.reparasuite.api.dto;

public record LoginResponse(
    String token,
    UsuarioResumenDto usuario
) { }
