package org.datamate.identity.role.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequest(
        @NotBlank(message = "{role.validation.name.required}")
        String name,

        String description
) {}

