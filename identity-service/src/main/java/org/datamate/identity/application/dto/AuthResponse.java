package org.datamate.identity.application.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String userName,
        String email
) {}
