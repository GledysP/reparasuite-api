package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TicketDetalleDto(
    UUID id,
    String estado,
    String asunto,
    String descripcion,
    List<MensajeDto> mensajes,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    UUID ordenTrabajoId,

    // ✅ snapshots del cliente (para backoffice/prefill)
    String clienteNombre,
    String clienteTelefono,
    String clienteEmail,

    // ✅ campos estructurados
    String equipo,
    String descripcionFalla,
    String tipoServicioSugerido,
    String direccion,
    String observaciones,

    // ✅ fotos
    List<TicketFotoDto> fotos,

    // ✅ categorías de trabajo (para backoffice/prefill)
    List<String> categoriasTrabajo
) {}