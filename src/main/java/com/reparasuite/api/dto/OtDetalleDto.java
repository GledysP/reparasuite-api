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

    String equipo,
    UUID equipoId,
    String equipoRegistradoNombre,
    UUID categoriaEquipoId,
    String categoriaEquipoNombre,
    String fallaReportada,

    String descripcion,

    String fallaDetectada,
    String diagnosticoTecnico,
    String trabajoARealizar,

    ClienteResumenDto cliente,
    UsuarioResumenDto tecnico,
    OffsetDateTime fechaPrevista,
    String direccion,
    String notasAcceso,
    List<NotaDto> notas,
    List<FotoDto> fotos,
    List<HistorialItemDto> historial,
    PresupuestoDto presupuesto,
    PagoDto pago,
    List<CitaDto> citas,
    List<MensajeDto> mensajes,
    List<String> categoriasTrabajo, 
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) { }