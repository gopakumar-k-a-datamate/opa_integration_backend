package org.datamate.identity.auth.application.dto;

import java.time.LocalDateTime;

public record LoginHistoryDto(
        String username,
        LocalDateTime loginTime,
        String status,
        String clientIp,
        String userAgent
) {}

