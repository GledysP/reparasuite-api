package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FotoDto(
    UUID id,
    String url,
    OffsetDateTime createdAt
) { }
