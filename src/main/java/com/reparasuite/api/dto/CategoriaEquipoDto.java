package com.reparasuite.api.dto;

import java.util.UUID;

public record CategoriaEquipoDto(
    UUID id,
    String codigo,
    String nombre,
    String descripcion,
    String icono
) {}