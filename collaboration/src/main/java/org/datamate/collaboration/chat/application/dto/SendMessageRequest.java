package org.datamate.collaboration.chat.application.dto;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Inbound DTO for sending a new message.
 * Supports text and/or pre-uploaded attachment URLs (RMS old behavior).
 */
public record SendMessageRequest(
        @Size(max = 1000, message = "Message text must not exceed 1000 characters")
        String text,

        List<String> attachmentUrls,

        UUID attachmentId
) {
    public SendMessageRequest(String text) {
        this(text, null, null);
    }

    public SendMessageRequest(String text, UUID attachmentId) {
        this(text, null, attachmentId);
    }

    public SendMessageRequest(String text, List<String> attachmentUrls) {
        this(text, attachmentUrls, null);
    }
}