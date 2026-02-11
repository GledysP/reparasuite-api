package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OtDetalleDto(
    UUID id,
    String codigo,
    String estado,
    String tipo,
    String prioridad,
    String descripcion,
    ClienteResumenDto cliente,
    UsuarioResumenDto tecnico,
    OffsetDateTime fechaPrevista,
    String direccion,
    String notasAcceso,
    List<NotaDto> notas,
    List<FotoDto> fotos,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) { }
