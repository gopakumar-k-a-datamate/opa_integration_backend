package org.datamate.collaboration.attachment.adapter.in.rest.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AttachmentResponse {
    private final String fileKey;
    private final String fileName;
    private final String contentType;
    private final long fileSize;
    private final String downloadUrl;
    private final LocalDateTime uploadedAt;
}