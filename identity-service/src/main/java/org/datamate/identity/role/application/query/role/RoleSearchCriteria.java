package org.datamate.identity.role.application.query.role;

import org.datamate.identity.role.domain.model.role.enums.RoleStatus;

public record RoleSearchCriteria(
    String search,
    RoleStatus status
) {}
