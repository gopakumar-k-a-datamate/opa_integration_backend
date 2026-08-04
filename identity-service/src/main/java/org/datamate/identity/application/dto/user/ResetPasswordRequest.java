package org.datamate.identity.application.dto.user;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Password must not be blank")
        String newPassword
) {}
