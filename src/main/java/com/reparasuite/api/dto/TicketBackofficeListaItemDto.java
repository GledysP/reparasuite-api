package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketBackofficeListaItemDto(
    UUID id,
    String estado,
    String asunto,
    OffsetDateTime updatedAt,
    UUID clienteId,
    String clienteNombre,
    String clienteEmail,

    // ✅ NUEVO: permite al front saber si ya existe OT vinculada
    UUID ordenTrabajoId
) {}