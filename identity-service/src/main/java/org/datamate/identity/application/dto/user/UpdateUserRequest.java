package org.datamate.identity.application.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "Username is mandatory")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String userName,

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
