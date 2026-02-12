package com.reparasuite.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioCrearRequest(
    @NotBlank String nombre,
    @NotBlank @Email String email,
    @NotNull String rol,      // "ADMIN" | "TECNICO"
    @NotBlank String password
) { }
