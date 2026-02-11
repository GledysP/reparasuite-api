package com.reparasuite.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ClienteCrearDto(
    String id,
    @NotBlank String nombre,
    String telefono,
    String email
) { }
