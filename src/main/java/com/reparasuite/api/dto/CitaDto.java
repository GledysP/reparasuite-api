package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CitaDto(
    UUID id,
    OffsetDateTime inicio,
    OffsetDateTime fin,
    String estado
) { }
