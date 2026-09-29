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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Application Service for sending messages.
 * <p>
 * Implements {@link SendMessageUseCase}. This handles the command side of messaging.
 * Supports sending pure text messages as well as messages with file attachments (matching RMS).
 */
@Service
@RequiredArgsConstructor
public class SendMessageService implements SendMessageUseCase {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB limit matching RMS
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
        if (request == null || ((request.text() == null || request.text().trim().isEmpty()) && request.attachmentId() == null)) {
            throw new DomainValidationException(
                    CollaborationErrorCodes.REQUIRED_FIELD_MISSING.code(), "text or attachmentId");
        }
        ensureThreadExists(threadId);

        boolean isFile = request.attachmentId() != null;
        Message message = Message.create(
                threadId,
                senderId,
                request.text(),
                isFile,
                false,
                request.attachmentId()
        );

        messageRepository.save(message);
    }

    @Override
    @Transactional
    public void sendMessageWithAttachment(UUID threadId, String senderId, String text, MultipartFile file) {
        if ((text == null || text.trim().isEmpty()) && (file == null || file.isEmpty())) {
            throw new DomainValidationException(
                    CollaborationErrorCodes.REQUIRED_FIELD_MISSING.code(), "text or file");
        }

        ensureThreadExists(threadId);

        UUID attachmentId = null;
        boolean isFile = false;

        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new DomainValidationException(
                        CollaborationErrorCodes.FIELD_BLANK.code(), "fileName");
            }

            String lower = originalFilename.toLowerCase();
            String matchedExtension = BLOCKED_EXTENSIONS.stream()
                    .filter(lower::endsWith)
                    .findFirst()
                    .orElse(null);
            if (matchedExtension != null) {
                throw new DomainValidationException(
                        CollaborationErrorCodes.FILE_TYPE_BLOCKED.code(), originalFilename, matchedExtension);
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new DomainValidationException(
                        CollaborationErrorCodes.FILE_SIZE_EXCEEDED.code(), originalFilename, file.getSize(), MAX_FILE_SIZE);
            }

            try {
                if (!storageService.bucketExists(defaultBucket)) {
                    storageService.createBucket(defaultBucket);
                }

                String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
                String objectKey = String.format("threads/%s/%s_%s", threadId, UUID.randomUUID(), sanitizedFilename);

                StorageObject storageObject = storageService.upload(
                        defaultBucket,
                        objectKey,
                        file.getInputStream(),
                        file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                        file.getSize()
                );

                Attachment attachment = Attachment.create(
                        originalFilename,
                        file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                        file.getSize(),
                        storageObject.getObjectKey(),
                        null
                );

                attachmentRepository.save(attachment);
                attachmentId = attachment.getId();
                isFile = true;
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file for upload: " + originalFilename, e);
            }
        }

        Message message = Message.create(
                threadId,
                senderId,
                text,
                isFile,
                false,
                attachmentId
        );

        messageRepository.save(message);
    }

    /**
     * Idempotent lazy Thread creation.
     */
    private void ensureThreadExists(UUID threadId) {
        if (!threadRepository.existsById(threadId)) {
            try {
                threadRepository.save(new Thread(threadId));
                logger.info("Lazily created thread [{}]", threadId);
            } catch (DataIntegrityViolationException e) {
                logger.debug("Thread [{}] was concurrently created by another transaction, proceeding.", threadId);
            }
        }
    }
}