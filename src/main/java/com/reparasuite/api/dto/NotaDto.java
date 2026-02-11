package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotaDto(
    UUID id,
    String contenido,
    OffsetDateTime createdAt
) { }
