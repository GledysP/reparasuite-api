package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CitaRequest(
    @NotBlank String inicio, // ISO-8601
    @NotBlank String fin     // ISO-8601
) { }
