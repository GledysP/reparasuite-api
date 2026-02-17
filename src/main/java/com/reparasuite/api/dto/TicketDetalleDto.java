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
    OffsetDateTime updatedAt
) { }
