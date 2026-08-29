package org.datamate.identity.role.application.query.role;

import org.datamate.identity.shared.model.RoleStatus;

public record RoleSearchCriteria(
    String search,
    RoleStatus status
) {}
