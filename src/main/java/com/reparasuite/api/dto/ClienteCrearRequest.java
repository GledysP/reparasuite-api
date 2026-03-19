package com.reparasuite.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteCrearRequest(
    @NotBlank
    @Size(max = 200)
    String nombre,

    @Size(max = 50)
    String telefono,

    @Email
    @Size(max = 200)
    String email
) {}