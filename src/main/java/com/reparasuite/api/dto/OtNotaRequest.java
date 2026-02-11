package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;

public record OtNotaRequest(@NotBlank String contenido) { }
