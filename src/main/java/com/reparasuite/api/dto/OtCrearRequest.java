package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtCrearRequest(
    @NotNull ClienteCrearDto cliente,
    @NotNull String tipo,        // "TIENDA" | "DOMICILIO"
    @NotNull String prioridad,   // "BAJA" | "MEDIA" | "ALTA"
    @NotBlank String descripcion,
    String tecnicoId,            // UUID como string o null
    String fechaPrevista,        // ISO-8601 o null
    String direccion,
    String notasAcceso
) { }
