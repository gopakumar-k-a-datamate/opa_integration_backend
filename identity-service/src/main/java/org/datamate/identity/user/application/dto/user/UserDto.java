package org.datamate.identity.user.application.dto.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.datamate.identity.shared.model.UserStatus;

public record UserDto(
        UUID id,
        String userName,
        String email,
        String phoneNumber,
        String firstName,
        String lastName,
        String referenceSystem,
        String referenceValue,
        String createdBy,
        LocalDateTime createdDate,
        UserStatus status,
        List<String> roles,
        boolean passwordTemporary
) {}
