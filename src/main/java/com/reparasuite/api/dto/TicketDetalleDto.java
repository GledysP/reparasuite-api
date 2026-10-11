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

    // Cliente real propietario del ticket.
    UUID clienteId,

    // Snapshots del cliente conservados para trazabilidad.
    String clienteNombre,
    String clienteTelefono,
    String clienteEmail,

    // Información estructurada de la solicitud.
    String equipo,
    String descripcionFalla,
    String tipoServicioSugerido,
    String direccion,
    String observaciones,

    // Evidencia original asociada al ticket.
    List<TicketFotoDto> fotos,

    // Servicios/categorías seleccionados por el cliente.
    List<String> categoriasTrabajo
) {
}