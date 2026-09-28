package org.datamate.collaboration.chat.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.dto.SendMessageRequest;
import org.datamate.collaboration.chat.application.port.in.SendMessageUseCase;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Message;
import org.datamate.collaboration.chat.domain.model.Thread;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Application Service for sending messages.
 * <p>
 * Implements {@link SendMessageUseCase}. This handles the command side of messaging.
 * <p>
 * <strong>Lazy Thread Creation:</strong> Because Domain Services generate the {@code threadId}
 * externally, the Chat Service does not expose a {@code POST /api/threads} endpoint.
 * Instead, the Thread record is automatically upserted the first time a message
 * is sent to it. A {@link DataIntegrityViolationException} catch handles the race
 * condition where two concurrent requests both attempt to create the same Thread.
 */
@Service
@RequiredArgsConstructor
public class SendMessageService implements SendMessageUseCase {

    @EnableLogger
    private Logger logger;

    private final MessageRepositoryPort messageRepository;
    private final ThreadRepositoryPort threadRepository;

    @Override
    @Transactional
    public void sendMessage(UUID threadId, String senderId, SendMessageRequest request) {
        ensureThreadExists(threadId);

        Message message = Message.create(
                threadId,
                senderId,
                request.text(),
                false,
                false,
                null
        );

        messageRepository.save(message);

        // TODO (Epic 2.2): Publish "New Message Saved" event to internal broker
        // for REST-to-WebSocket fanout across all server nodes.
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
