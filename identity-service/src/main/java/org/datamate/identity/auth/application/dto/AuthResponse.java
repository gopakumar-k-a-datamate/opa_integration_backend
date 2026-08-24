package org.datamate.identity.auth.application.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String userName,
        String email
) {}

