package org.datamate.identity.application.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Password must not be blank")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String newPassword
) {}
