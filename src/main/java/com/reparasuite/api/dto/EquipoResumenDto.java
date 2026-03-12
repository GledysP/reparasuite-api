package com.reparasuite.api.dto;

import java.util.UUID;

public record EquipoResumenDto(
    UUID id,
    String codigoEquipo,
    UUID clienteId,
    String clienteNombre,
    UUID categoriaEquipoId,
    String categoriaEquipoNombre,
    String tipoEquipo,
    String marca,
    String modelo,
    String numeroSerie,
    String ubicacionHabitual,
    boolean estadoActivo
) {}