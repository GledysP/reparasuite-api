package com.reparasuite.api.dto;

import java.util.UUID;

public record InventarioCategoriaDto(
    UUID id,
    String codigo,
    String nombre,
    String descripcion
) {}