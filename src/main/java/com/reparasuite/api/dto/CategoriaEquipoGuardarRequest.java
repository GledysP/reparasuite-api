package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaEquipoGuardarRequest(
    @NotBlank @Size(max = 50) String codigo,
    @NotBlank @Size(max = 120) String nombre,
    @Size(max = 2000) String descripcion,
    @Size(max = 50) String icono,
    Integer ordenVisual,
    Boolean activa
) {}