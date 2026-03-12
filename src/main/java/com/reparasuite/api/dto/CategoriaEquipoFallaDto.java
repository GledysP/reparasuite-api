package com.reparasuite.api.dto;

import java.util.UUID;

public record CategoriaEquipoFallaDto(
    UUID id,
    String codigo,
    String nombre,
    String descripcion
) {}