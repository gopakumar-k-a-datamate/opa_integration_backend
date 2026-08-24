package org.datamate.identity.user.application.query;

import org.datamate.identity.user.shared.model.UserStatus;

public record UserSearchCriteria(
    String search,
    String role,
    UserStatus status
) {}


