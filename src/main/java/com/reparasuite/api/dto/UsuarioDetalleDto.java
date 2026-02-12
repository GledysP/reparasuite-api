package com.reparasuite.api.dto;

import java.util.UUID;

public record UsuarioDetalleDto(
    UUID id,
    String nombre,
    String usuario,
    String email,
    String rol,
    boolean activo
) { }
