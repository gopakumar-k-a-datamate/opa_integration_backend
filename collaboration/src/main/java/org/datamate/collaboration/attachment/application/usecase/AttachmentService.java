package org.datamate.collaboration.attachment.application.usecase;

import com.datamate.bedrock.framework.storage.application.port.StorageService;
import com.datamate.bedrock.framework.storage.domain.model.StorageObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datamate.collaboration.attachment.application.port.in.UploadAttachmentUseCase;
import org.datamate.collaboration.attachment.domain.model.AttachmentMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService implements UploadAttachmentUseCase {

    private final StorageService storageService;

    @Value("${bedrock.storage.minio.bucket:chat-attachments}")
    private String defaultBucket;

    @Override
    public AttachmentMetadata upload(InputStream inputStream, String originalFilename, String contentType, long size, UUID threadId) {
        ensureBucketExists(defaultBucket);

        String sanitizedFilename = (originalFilename != null && !originalFilename.isBlank())
                ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "unnamed_file";

        String objectKey = (threadId != null)
                ? String.format("threads/%s/%s_%s", threadId, UUID.randomUUID(), sanitizedFilename)
                : String.format("attachments/%s_%s", UUID.randomUUID(), sanitizedFilename);

        log.info("Uploading chat attachment '{}' to bucket '{}'", objectKey, defaultBucket);
        StorageObject storedObject = storageService.upload(defaultBucket, objectKey, inputStream, contentType, size);

        return AttachmentMetadata.builder()
                .fileKey(storedObject.getObjectKey())
                .fileName(sanitizedFilename)
                .contentType(contentType)
                .fileSize(size)
                .bucketName(defaultBucket)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public InputStream download(String fileKey) {
        log.info("Downloading chat attachment '{}' from bucket '{}'", fileKey, defaultBucket);
        return storageService.download(defaultBucket, fileKey);
    }

    private void ensureBucketExists(String bucketName) {
        try {
            if (!storageService.bucketExists(bucketName)) {
                log.info("Creating bucket '{}' as it does not exist", bucketName);
                storageService.createBucket(bucketName);
            }
        } catch (Exception e) {
            log.warn("Bucket check/creation error for '{}': {}", bucketName, e.getMessage());
        }
    }
}