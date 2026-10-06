package org.datamate.collaboration.chat.adapter.out.persistence.message.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.datamate.collaboration.chat.adapter.out.persistence.thread.entity.ThreadJpaEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for the {@code chat_messages} table.
 * <p>
 * Uses a raw {@code threadId} UUID column instead of a {@code @ManyToOne}
 * relationship to {@link ThreadJpaEntity}
 * to enforce aggregate boundaries and eliminate N+1 query problems
 * when fetching paginated message history.
 * <p>
 * Used exclusively by the persistence adapter layer.
 */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "thread_id", nullable = false, updatable = false)
    private UUID threadId;

    @Column(name = "sender_id", nullable = false)
    private String senderId;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "is_file", nullable = false)
    private boolean file;

    @Column(name = "is_system_message", nullable = false)
    private boolean systemMessage;

    @Column(name = "attachment_id")
    private UUID attachmentId;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getThreadId() { return threadId; }
    public void setThreadId(UUID threadId) { this.threadId = threadId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isFile() { return file; }
    public void setFile(boolean file) { this.file = file; }
    public boolean isSystemMessage() { return systemMessage; }
    public void setSystemMessage(boolean systemMessage) { this.systemMessage = systemMessage; }
    public UUID getAttachmentId() { return attachmentId; }
    public void setAttachmentId(UUID attachmentId) { this.attachmentId = attachmentId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}