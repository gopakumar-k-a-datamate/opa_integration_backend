package org.datamate.identity.role.application.query;

import org.datamate.identity.role.shared.model.RoleStatus;

public record RoleSearchCriteria(
    String search,
    RoleStatus status
) {}


