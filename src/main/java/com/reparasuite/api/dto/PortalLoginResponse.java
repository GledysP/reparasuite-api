package com.reparasuite.api.dto;

public record PortalLoginResponse(
    String accessToken,
    String refreshToken,
    long expiresInSeconds
) {
}