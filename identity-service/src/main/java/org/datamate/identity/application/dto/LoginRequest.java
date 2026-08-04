package org.datamate.identity.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username is required")
        String userName,

        @NotBlank(message = "Password is required")
        String password
) {}
