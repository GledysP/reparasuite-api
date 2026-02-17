package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketCrearRequest(
    @NotBlank String asunto,
    @NotBlank String descripcion
) { }
