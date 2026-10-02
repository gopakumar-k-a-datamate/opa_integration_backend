package org.datamate.collaboration.chat.application.dto;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * @deprecated Use {@link SendMessageCommand} to strictly adhere to CQRS naming conventions.
 */
@Deprecated
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

    public SendMessageCommand toCommand() {
        return new SendMessageCommand(text, attachmentUrls, attachmentId);
    }
}