package org.datamate.identity.role.application.dto.role;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
        @NotBlank(message = "{role.validation.name.required}")
        String name,

        String description
) {}
