package org.datamate.collaboration.chat.domain.model;

import org.datamate.collaboration.exception.CollaborationErrorCodes;
import org.datamate.collaboration.exception.DomainValidationException;

import java.util.UUID;

/**
 * Attachment Domain Entity.
 * <p>
 * Represents a file uploaded to the chat system. Each Attachment has a 1-to-1
 * relationship with a {@link Message} (linked via {@code attachmentId}).
 * The message's {@code text} field serves as the attachment's caption.
 * <p>
 * {@code previewUrl} is nullable and populated asynchronously when non-PDF
 * documents are converted to preview PDFs (Epic 6 — Parked Feature).
 * <p>
 * Pure Domain Entity — no Spring, JPA, or framework annotations.
 */
public class Attachment {

    private final UUID id;
    private final String fileName;
    private final String mimeType;
    private final long fileSize;
    private final String uploadUrl;
    private final String previewUrl;

    private Attachment(Builder builder) {
        requireNonNull(builder.id, "attachmentId");
        requireNonNull(builder.fileName, "fileName");
        requireNonNull(builder.mimeType, "mimeType");
        requireNonNull(builder.uploadUrl, "uploadUrl");

        this.id = builder.id;
        this.fileName = builder.fileName;
        this.mimeType = builder.mimeType;
        this.fileSize = builder.fileSize;
        this.uploadUrl = builder.uploadUrl;
        this.previewUrl = builder.previewUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() { return id; }
    public String getFileName() { return fileName; }
    public String getMimeType() { return mimeType; }
    public long getFileSize() { return fileSize; }
    public String getUploadUrl() { return uploadUrl; }
    public String getPreviewUrl() { return previewUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attachment that)) return false;
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
        private String fileName;
        private String mimeType;
        private long fileSize;
        private String uploadUrl;
        private String previewUrl;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public Builder fileSize(long fileSize) { this.fileSize = fileSize; return this; }
        public Builder uploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; return this; }
        public Builder previewUrl(String previewUrl) { this.previewUrl = previewUrl; return this; }

        public Attachment build() {
            return new Attachment(this);
        }
    }
}
