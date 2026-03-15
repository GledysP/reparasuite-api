package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtCrearRequest(
    @NotNull ClienteCrearDto cliente,

    @NotNull String tipo,
    @NotNull String prioridad,

    String equipo,
    String equipoId,
    String categoriaEquipoId,
    String fallaReportada,

    String ticketId,

    @NotBlank String descripcion,

    String tecnicoId,
    String fechaPrevista,
    String direccion,
    String notasAcceso
) { }