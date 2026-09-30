package org.datamate.collaboration.chat.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Inbound DTO for sending a new message.
 * Only carries the text payload — {@code threadId} and {@code senderId}
 * are extracted from the authenticated security principal (Ticket JWT).
 */
public record SendMessageRequest(
        @NotBlank(message = "Message text must not be blank")
        String text
) {
}
