package org.datamate.collaboration.chat.application.usecase;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.dto.MessageDto;
import org.datamate.collaboration.chat.application.dto.SendMessageCommand;
import org.datamate.collaboration.chat.application.dto.SendMessageRequest;
import org.datamate.collaboration.chat.application.mapper.MessageMapper;
import org.datamate.collaboration.chat.application.port.in.SendMessageUseCase;
import org.datamate.collaboration.chat.application.port.out.AttachmentRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.MessageBroadcastPort;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Attachment;
import org.datamate.collaboration.chat.domain.model.Message;
import org.datamate.collaboration.chat.domain.model.Thread;
import org.datamate.collaboration.exception.ApplicationValidationException;
import org.datamate.collaboration.exception.CollaborationErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SendMessageService implements SendMessageUseCase {

    private final Logger logger = LoggerFactory.getLogger(SendMessageService.class);

    private final MessageRepositoryPort messageRepository;
    private final ThreadRepositoryPort threadRepository;
    private final AttachmentRepositoryPort attachmentRepository;
    private final MessageMapper messageMapper;
    private final MessageBroadcastPort messageBroadcastPort;

    @Override
    @Transactional
    public void sendMessage(UUID threadId, String senderId, SendMessageCommand command) {
        validateCommand(command);

        ensureThreadExists(threadId);

        UUID attachmentId = resolveAttachmentId(command);
        boolean isFile = attachmentId != null;

        Message message = Message.create(
                threadId,
                senderId,
                command.text(),
                isFile,
                false,
                attachmentId
        );

        Message savedMessage = messageRepository.save(message);

        Attachment attachment = null;
        if (attachmentId != null) {
            attachment = attachmentRepository.findById(attachmentId).orElse(null);
        }

        MessageDto messageDto = messageMapper.toDto(savedMessage != null ? savedMessage : message, attachment);
        messageBroadcastPort.broadcastMessage(threadId, messageDto);
    }

    @Override
    @Transactional
    public void sendMessage(UUID threadId, String senderId, SendMessageRequest request) {
        sendMessage(threadId, senderId, request != null ? request.toCommand() : null);
    }

    private void validateCommand(SendMessageCommand command) {
        if (command == null) {
            throw new ApplicationValidationException(
                    CollaborationErrorCodes.REQUIRED_FIELD_MISSING.code(), "command");
        }

        boolean hasText = command.text() != null && !command.text().trim().isEmpty();
        boolean hasAttachmentId = command.attachmentId() != null;
        boolean hasAttachmentUrls = command.attachmentUrls() != null && !command.attachmentUrls().isEmpty();

        if (!hasText && !hasAttachmentId && !hasAttachmentUrls) {
            throw new ApplicationValidationException(
                    CollaborationErrorCodes.REQUIRED_FIELD_MISSING.code(), "text or attachment");
        }
    }

        private UUID resolveAttachmentId(SendMessageCommand command) {
        if (command.attachmentId() != null) {
            return command.attachmentId();
        }
        if (command.attachmentUrls() != null && !command.attachmentUrls().isEmpty()) {
            String url = command.attachmentUrls().get(0);
            
            try {
                // If it's just a UUID string passed in an array, parse it
                String idPart = url.substring(url.lastIndexOf('/') + 1);
                return UUID.fromString(idPart);
            } catch (Exception e) {
                // Not a UUID. It's a raw MinIO URL.
                String objectKey = url.substring(url.lastIndexOf('/') + 1);
                String friendlyFileName = objectKey;
                
                int firstUnderscore = objectKey.indexOf('_');
                if (firstUnderscore != -1) {
                    int secondUnderscore = objectKey.indexOf('_', firstUnderscore + 1);
                    if (objectKey.startsWith("br_") && secondUnderscore != -1) {
                        friendlyFileName = objectKey.substring(secondUnderscore + 1);
                    } else if (!objectKey.startsWith("br_")) {
                        friendlyFileName = objectKey.substring(firstUnderscore + 1);
                    }
                }
                
                String mimeType = "application/octet-stream";
                String lower = friendlyFileName.toLowerCase();
                if (lower.endsWith(".png")) mimeType = "image/png";
                else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) mimeType = "image/jpeg";
                else if (lower.endsWith(".pdf")) mimeType = "application/pdf";
                
                Attachment attachment = Attachment.create(
                        friendlyFileName,
                        mimeType,
                        0L,
                        url,
                        null
                );
                attachmentRepository.save(attachment);
                logger.info("Created missing Attachment entity on the fly for URL: {}", url);
                return attachment.getId();
            }
        }
        return null;
    }

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

