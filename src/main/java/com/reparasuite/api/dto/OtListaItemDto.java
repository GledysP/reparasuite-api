package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;

public record OtListaItemDto(
    UUID id,
    String codigo,
    String estado,
    String tipo,
    String prioridad,

    String equipo,
    UUID equipoId,
    UUID categoriaEquipoId,
    String categoriaEquipoNombre,
    String fallaReportada,

    String clienteNombre,
    String tecnicoNombre,
    List<String> categoriasTrabajo, // ✅ NUEVO CAMPO PARA LOS CHIPS
    OffsetDateTime updatedAt
) { }