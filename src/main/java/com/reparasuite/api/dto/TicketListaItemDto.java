package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketListaItemDto(
    UUID id,
    String estado,
    String asunto,
    OffsetDateTime updatedAt
) { }
