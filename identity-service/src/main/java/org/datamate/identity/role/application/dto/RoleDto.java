package org.datamate.identity.role.application.dto;

import org.datamate.identity.role.shared.model.RoleStatus;
import java.util.UUID;

public record RoleDto(
        UUID id,
        String name,
        String description,
        RoleStatus status
) {}



