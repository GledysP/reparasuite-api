package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MensajeDto(
    UUID id,
    String remitenteTipo,
    String remitenteNombre,
    String contenido,
    OffsetDateTime createdAt
) { }
