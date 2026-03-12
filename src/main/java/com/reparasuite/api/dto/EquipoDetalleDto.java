package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EquipoDetalleDto(
    UUID id,
    String codigoEquipo,
    UUID clienteId,
    String clienteNombre,
    CategoriaEquipoDto categoria,
    String codigoInterno,
    String tipoEquipo,
    String marca,
    String modelo,
    String numeroSerie,
    String descripcionGeneral,
    String fechaCompra,
    String garantiaHasta,
    String ubicacionHabitual,
    String notasTecnicas,
    boolean estadoActivo,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}