package org.datamate.collaboration.attachment.application.port.in;

import org.datamate.collaboration.attachment.domain.model.AttachmentMetadata;

import java.io.InputStream;
import java.util.UUID;

public interface UploadAttachmentUseCase {
    AttachmentMetadata upload(InputStream inputStream, String originalFilename, String contentType, long size, UUID threadId);
    InputStream download(String fileKey);
}