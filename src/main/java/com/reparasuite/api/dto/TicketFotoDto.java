package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketFotoDto(
    UUID id,
    String url,
    String nombreOriginal,
    OffsetDateTime createdAt
) {}