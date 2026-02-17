package com.reparasuite.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PortalLoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) { }
