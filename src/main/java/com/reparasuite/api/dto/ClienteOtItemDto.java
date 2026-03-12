package com.reparasuite.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClienteOtItemDto(
	    UUID id,
	    String codigo,
	    String estado,
	    String tipo,
	    OffsetDateTime updatedAt,
	    String tecnicoNombre
	) {}
