package org.datamate.collaboration.attachment.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AttachmentMetadata {
    private final String fileKey;
    private final String fileName;
    private final String contentType;
    private final long fileSize;
    private final String bucketName;
    private final LocalDateTime uploadedAt;
}