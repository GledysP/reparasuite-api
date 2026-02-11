package com.reparasuite.api.dto;

import java.util.UUID;

public record ClienteResumenDto(
    UUID id,
    String nombre,
    String telefono,
    String email
) { }
