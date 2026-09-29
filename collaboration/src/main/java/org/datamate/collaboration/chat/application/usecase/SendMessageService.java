package org.datamate.collaboration.chat.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.datamate.bedrock.framework.storage.application.port.StorageService;
import com.datamate.bedrock.framework.storage.domain.model.StorageObject;
import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.dto.SendMessageRequest;
import org.datamate.collaboration.chat.application.port.in.SendMessageUseCase;
import org.datamate.collaboration.chat.application.port.out.AttachmentRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Attachment;
import org.datamate.collaboration.chat.domain.model.Message;
import org.datamate.collaboration.chat.domain.model.Thread;
import org.datamate.collaboration.exception.CollaborationErrorCodes;
import org.datamate.collaboration.exception.DomainValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application Service for sending messages.
 * <p>
 * Implements {@link SendMessageUseCase}. Handles the command side of messaging.
 * Follows the RMS standard: the frontend uploads files to cloud storage (MinIO) first,
 * and sends a JSON payload containing the message text and/or attachmentUrls.
 */
@Service
@RequiredArgsConstructor
public class SendMessageService implements SendMessageUseCase {

    private static final List<String> BLOCKED_EXTENSIONS = List.of(".xml", ".bpmn", ".exe", ".bat", ".sh");

    @EnableLogger
    private Logger logger;

    private final MessageRepositoryPort messageRepository;
    private final ThreadRepositoryPort threadRepository;
    private final AttachmentRepositoryPort attachmentRepository;
    private final StorageService storageService;

    @Value("${bedrock.storage.minio.bucket:chat-attachments}")
    private String defaultBucket;

    @Override
    @Transactional
    public void sendMessage(UUID threadId, String senderId, SendMessageRequest request) {
        if (request == null) {
            throw new DomainValidationException(
                    CollaborationErrorCodes.REQUIRED_FIELD_MISSING.code(), "request");
        }

        boolean hasText = request.text() != null && !request.text().trim().isEmpty();
        boolean hasAttachmentUrls = request.attachmentUrls() != null && !request.attachmentUrls().isEmpty();
        boolean hasAttachmentId = request.attachmentId() != null;

        if (!hasText && !hasAttachmentUrls && !hasAttachmentId) {
            throw new DomainValidationException(
                    CollaborationErrorCodes.REQUIRED_FIELD_MISSING.code(), "text or attachmentUrls");
        }

        ensureThreadExists(threadId);

        UUID resolvedAttachmentId = request.attachmentId();
        boolean isFile = resolvedAttachmentId != null;

        if (hasAttachmentUrls) {
            String attachmentUrl = request.attachmentUrls().get(0);
            if (attachmentUrl == null || attachmentUrl.isBlank()) {
                throw new DomainValidationException(
                        CollaborationErrorCodes.FIELD_BLANK.code(), "attachmentUrl");
            }

            String objectKeyOrFileName = attachmentUrl.contains("/")
                    ? attachmentUrl.substring(attachmentUrl.lastIndexOf('/') + 1)
                    : attachmentUrl;

            // Security check: validate against blocked extensions
            String lower = objectKeyOrFileName.toLowerCase();
            String matchedExtension = BLOCKED_EXTENSIONS.stream()
                    .filter(lower::endsWith)
                    .findFirst()
                    .orElse(null);
            if (matchedExtension != null) {
                throw new DomainValidationException(
                        CollaborationErrorCodes.FILE_TYPE_BLOCKED.code(), objectKeyOrFileName, matchedExtension);
            }

            // Extract friendly filename without timestamp prefix if present
            String fileName = objectKeyOrFileName.contains("_")
                    ? objectKeyOrFileName.substring(objectKeyOrFileName.indexOf('_') + 1)
                    : objectKeyOrFileName;

            String contentType = inferContentType(fileName);
            long fileSize = 0L;

            try {
                StorageObject metadata = storageService.getMetadata(defaultBucket, objectKeyOrFileName);
                if (metadata != null) {
                    if (metadata.getContentType() != null) {
                        contentType = metadata.getContentType();
                    }
                    fileSize = metadata.getSize();
                }
            } catch (Exception e) {
                if (logger != null) {
                    logger.debug("Could not fetch metadata for objectKey: {}. Using inferred values.", objectKeyOrFileName);
                }
            }

            Attachment attachment = Attachment.create(
                    fileName,
                    contentType,
                    fileSize,
                    attachmentUrl,
                    null
            );

            attachmentRepository.save(attachment);
            resolvedAttachmentId = attachment.getId();
            isFile = true;
        }

        Message message = Message.create(
                threadId,
                senderId,
                request.text(),
                isFile,
                false,
                resolvedAttachmentId
        );

        messageRepository.save(message);
    }

    private String inferContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    /**
     * Idempotent lazy Thread creation.
     */
    private void ensureThreadExists(UUID threadId) {
        if (!threadRepository.existsById(threadId)) {
            try {
                threadRepository.save(new Thread(threadId));
                if (logger != null) {
                    logger.info("Lazily created thread [{}]", threadId);
                }
            } catch (DataIntegrityViolationException e) {
                if (logger != null) {
                    logger.debug("Thread [{}] was concurrently created by another transaction, proceeding.", threadId);
                }
            }
        }
    }
}