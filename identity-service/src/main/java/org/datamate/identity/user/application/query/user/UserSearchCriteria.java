package org.datamate.identity.user.application.query.user;

import org.datamate.identity.user.domain.model.user.enums.UserStatus;

public record UserSearchCriteria(
    String search,
    String role,
    UserStatus status
) {}
