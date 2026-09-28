package org.datamate.collaboration.chat.domain.model;

import org.datamate.collaboration.exception.CollaborationErrorCodes;
import org.datamate.collaboration.exception.DomainValidationException;

import java.time.Instant;
import java.util.UUID;

/**
 * Message Domain Entity.
 * <p>
 * Represents a single message within a {@link Thread}. Uses a raw {@code threadId}
 * (UUID foreign key) instead of a JPA entity reference to avoid N+1 queries and
 * keep aggregate boundaries clean.
 * <p>
 * Supports a 1-to-1 attachment model: {@code attachmentId} optionally links to an
 * {@link Attachment}, and {@code text} serves as the caption when {@code isFile} is true.
 * <p>
 * Pure Domain Entity — no Spring, JPA, or framework annotations.
 */
public class Message {

    private final UUID id;
    private final UUID threadId;
    private final String senderId;
    private final String text;
    private final boolean file;
    private final boolean systemMessage;
    private final UUID attachmentId;
    private final Instant timestamp;

    private Message(Builder builder) {
        requireNonNull(builder.id, "messageId");
        requireNonNull(builder.threadId, "threadId");
        requireNonNull(builder.senderId, "senderId");
        requireNonNull(builder.timestamp, "timestamp");

        this.id = builder.id;
        this.threadId = builder.threadId;
        this.senderId = builder.senderId;
        this.text = builder.text;
        this.file = builder.file;
        this.systemMessage = builder.systemMessage;
        this.attachmentId = builder.attachmentId;
        this.timestamp = builder.timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() { return id; }
    public UUID getThreadId() { return threadId; }
    public String getSenderId() { return senderId; }
    public String getText() { return text; }
    public boolean isFile() { return file; }
    public boolean isSystemMessage() { return systemMessage; }
    public UUID getAttachmentId() { return attachmentId; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Domain-level guard: throws {@link DomainValidationException} with
     * the reusable {@code REQUIRED_FIELD_MISSING} error code.
     */
    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new DomainValidationException(
                    CollaborationErrorCodes.REQUIRED_FIELD_MISSING.code(), fieldName);
        }
    }

    public static class Builder {
        private UUID id;
        private UUID threadId;
        private String senderId;
        private String text;
        private boolean file;
        private boolean systemMessage;
        private UUID attachmentId;
        private Instant timestamp;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder threadId(UUID threadId) { this.threadId = threadId; return this; }
        public Builder senderId(String senderId) { this.senderId = senderId; return this; }
        public Builder text(String text) { this.text = text; return this; }
        public Builder file(boolean file) { this.file = file; return this; }
        public Builder systemMessage(boolean systemMessage) { this.systemMessage = systemMessage; return this; }
        public Builder attachmentId(UUID attachmentId) { this.attachmentId = attachmentId; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public Message build() {
            return new Message(this);
        }
    }
}
