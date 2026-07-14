package org.datamate.identity.application.dto.authorization;

import java.util.UUID;

public record PermissionNamespaceDto(
    UUID id,
    String name,
    String description
) {}
