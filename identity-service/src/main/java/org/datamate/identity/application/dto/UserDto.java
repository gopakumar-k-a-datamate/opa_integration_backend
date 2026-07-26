package org.datamate.identity.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,
        String userName,
        String email,
        String phoneNumber,
        String firstName,
        String lastName,
        String createdBy,
        LocalDateTime createdDate
) {}
