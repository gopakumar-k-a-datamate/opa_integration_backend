package org.datamate.collaboration.chat.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
public class MessageJpaEntity {
    @Id
    private UUID id;
    private UUID threadId;
    private String senderId;
    private String text;
    private boolean isFile;
    private boolean isSystemMessage;
    private UUID attachmentId;
    private Instant timestamp;
}
