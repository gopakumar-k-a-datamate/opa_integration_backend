package org.datamate.identity.application.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Invalid email format")
        String email,

        String phoneNumber,

        @NotBlank(message = "First name is mandatory")
        String firstName,

        @NotBlank(message = "Last name is mandatory")
        String lastName,

        String referenceSystem,
        String referenceValue
) {}
