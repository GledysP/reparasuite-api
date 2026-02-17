package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketEstadoUpdateRequest(
    @NotBlank String estado
) {}
