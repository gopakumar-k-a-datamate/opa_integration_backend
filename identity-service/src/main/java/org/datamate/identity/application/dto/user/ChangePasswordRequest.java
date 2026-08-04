package org.datamate.identity.application.dto.user;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Old password must not be blank")
        String oldPassword,

        @NotBlank(message = "New password must not be blank")
        String newPassword
) {}
