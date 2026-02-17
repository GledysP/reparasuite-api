package com.reparasuite.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioCrearRequest(
    @NotBlank String nombre,
    @NotBlank String usuario,
    @NotBlank @Email String email,
    @NotBlank String rol,      // ADMIN|TECNICO
    @NotBlank String password
) { }
