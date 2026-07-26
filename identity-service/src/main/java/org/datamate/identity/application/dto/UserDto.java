package org.datamate.identity.application.dto;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String userName,
        String email,
        String phoneNumber,
        String firstName,
        String lastName,
        String createdBy,
        LocalDateTime createdDate
) {}
