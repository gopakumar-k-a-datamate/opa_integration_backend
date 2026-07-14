package org.datamate.identity.application.dto.authorization;

import java.util.UUID;

public record PermissionDto(
    UUID id,
    UUID resourceId,
    String action,
    String code,
    String description
) {}
