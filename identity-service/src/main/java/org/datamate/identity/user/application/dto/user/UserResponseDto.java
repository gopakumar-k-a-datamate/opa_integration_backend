package org.datamate.identity.user.application.dto.user;

import org.datamate.identity.user.domain.model.user.enums.UserStatus;
import java.util.List;
import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String userName,
    String email,
    String firstName,
    String lastName,
    UserStatus status,
    List<String> roles
) {}
