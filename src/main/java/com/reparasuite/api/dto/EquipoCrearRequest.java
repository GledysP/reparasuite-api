package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipoCrearRequest(
    @NotBlank String clienteId,
    String categoriaEquipoId,
    @Size(max = 50) String codigoInterno,
    @Size(max = 120) String tipoEquipo,
    @Size(max = 120) String marca,
    @Size(max = 120) String modelo,
    @Size(max = 120) String numeroSerie,
    @Size(max = 4000) String descripcionGeneral,
    String fechaCompra,
    String garantiaHasta,
    @Size(max = 255) String ubicacionHabitual,
    @Size(max = 4000) String notasTecnicas,
    Boolean estadoActivo
) {}