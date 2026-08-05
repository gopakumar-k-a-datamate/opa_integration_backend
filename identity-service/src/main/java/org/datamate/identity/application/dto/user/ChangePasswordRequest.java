package org.datamate.identity.application.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Old password must not be blank")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String oldPassword,

        @NotBlank(message = "New password must not be blank")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String newPassword
) {}
