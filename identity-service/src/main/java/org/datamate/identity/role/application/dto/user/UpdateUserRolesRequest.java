package org.datamate.identity.role.application.dto.user;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateUserRolesRequest(
        @NotNull(message = "{user.validation.roles.required}")
        List<String> roles
) {}

