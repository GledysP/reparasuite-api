package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;

public record MensajeEnviarRequest(
    @NotBlank String contenido
) { }
