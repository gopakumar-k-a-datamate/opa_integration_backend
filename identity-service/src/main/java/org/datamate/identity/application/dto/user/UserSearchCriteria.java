package org.datamate.identity.application.dto.user;

import org.datamate.identity.shared.model.UserStatus;

public record UserSearchCriteria(
    String search,
    String role,
    UserStatus status
) {}
