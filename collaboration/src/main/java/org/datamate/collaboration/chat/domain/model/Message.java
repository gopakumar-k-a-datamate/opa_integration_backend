package org.datamate.collaboration.chat.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Message {
    private final UUID id;
    private final UUID threadId;
    private final String senderId;
    private final String text;
    private final boolean isFile;
    private final boolean isSystemMessage;
    private final UUID attachmentId;
    private final Instant timestamp;

    public static Message createNew(UUID threadId, String senderId, String text, boolean isSystemMessage) {
        return Message.builder()
                .id(UUID.randomUUID())
                .threadId(threadId)
                .senderId(senderId)
                .text(text)
                .isFile(false)
                .isSystemMessage(isSystemMessage)
                .timestamp(Instant.now())
                .build();
    }
}
