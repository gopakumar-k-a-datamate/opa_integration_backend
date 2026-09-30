package org.datamate.collaboration.chat.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound DTO representing a single message in API responses.
 * Maps from the domain {@link org.datamate.collaboration.chat.domain.model.Message}.
 */
public record MessageDto(
        UUID id,
        String senderId,
        String text,
        boolean isFile,
        boolean isSystemMessage,
        UUID attachmentId,
        Instant timestamp
) {
}
