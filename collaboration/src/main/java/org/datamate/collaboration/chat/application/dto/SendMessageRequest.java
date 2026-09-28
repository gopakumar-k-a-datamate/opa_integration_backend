package org.datamate.collaboration.chat.application.dto;

import java.util.UUID;

/**
 * Inbound DTO for sending a new message.
 * Supports text and optional attachmentId.
 */
public record SendMessageRequest(
        String text,
        UUID attachmentId
) {
    public SendMessageRequest {
        if ((text == null || text.trim().isEmpty()) && attachmentId == null) {
            throw new IllegalArgumentException("Message text or attachmentId must be provided");
        }
    }

    public SendMessageRequest(String text) {
        this(text, null);
    }
}