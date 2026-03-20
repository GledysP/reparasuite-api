package com.reparasuite.api.dto;

public record TokenPairResponse(
    String accessToken,
    String refreshToken,
    long expiresInSeconds
) {
}