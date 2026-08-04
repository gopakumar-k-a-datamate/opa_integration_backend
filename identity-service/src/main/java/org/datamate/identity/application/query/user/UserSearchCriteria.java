package org.datamate.identity.application.query.user;

import org.datamate.identity.shared.model.UserStatus;

public record UserSearchCriteria(
    String search,
    String role,
    UserStatus status
) {}
