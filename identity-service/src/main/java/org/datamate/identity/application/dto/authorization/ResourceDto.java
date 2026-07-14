package org.datamate.identity.application.dto.authorization;

import java.util.UUID;

public record ResourceDto(
    UUID id,
    UUID namespaceId,
    String name,
    String description
) {}
