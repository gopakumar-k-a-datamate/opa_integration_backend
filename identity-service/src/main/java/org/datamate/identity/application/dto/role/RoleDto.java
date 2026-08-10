package org.datamate.identity.application.dto.role;

import org.datamate.identity.shared.model.RoleStatus;
import java.util.UUID;

public record RoleDto(
        UUID id,
        String name,
        String description,
        RoleStatus status
) {}

