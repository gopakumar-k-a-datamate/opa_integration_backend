package org.datamate.identity.application.query.role;

import org.datamate.identity.shared.model.RoleStatus;

public record RoleSearchCriteria(
    String search,
    RoleStatus status
) {}
