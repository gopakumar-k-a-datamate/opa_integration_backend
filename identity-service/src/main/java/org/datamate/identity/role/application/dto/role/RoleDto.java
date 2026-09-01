package org.datamate.identity.role.application.dto.role;

import org.datamate.identity.role.domain.model.role.enums.RoleStatus;
import java.util.UUID;

public record RoleDto(
        UUID id,
        String name,
        String description,
        RoleStatus status
) {}

