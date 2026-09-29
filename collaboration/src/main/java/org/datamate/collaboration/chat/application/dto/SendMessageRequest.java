package org.datamate.collaboration.chat.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Inbound DTO for sending a new message.
 * Supports text payload and optional attachment reference.
 */
public record SendMessageRequest(
        @NotBlank(message = "Message text must not be blank")
        @Size(max = 1000, message = "Message text must not exceed 1000 characters")
        String text,

        UUID attachmentId
) {
    public SendMessageRequest(String text) {
        this(text, null);
    }
}