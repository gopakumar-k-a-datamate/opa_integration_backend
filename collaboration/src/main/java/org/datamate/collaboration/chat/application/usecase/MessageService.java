package org.datamate.collaboration.chat.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.port.in.GetMessagesUseCase;
import org.datamate.collaboration.chat.application.port.in.SendMessageUseCase;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Message;
import org.datamate.collaboration.chat.domain.model.Thread;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Application Service orchestrating the core messaging use cases.
 * <p>
 * Implements both {@link SendMessageUseCase} and {@link GetMessagesUseCase}.
 * This is the single entry point for all messaging business logic, including
 * the lazy, idempotent Thread creation strategy defined in the Architecture Specification.
 * <p>
 * <strong>Lazy Thread Creation:</strong> Because Domain Services generate the {@code threadId}
 * externally, the Chat Service does not expose a {@code POST /api/threads} endpoint.
 * Instead, the Thread record is automatically upserted the first time a message
 * is sent to it. A {@link DataIntegrityViolationException} catch handles the race
 * condition where two concurrent requests both attempt to create the same Thread.
 */
@Service
@RequiredArgsConstructor
public class MessageService implements SendMessageUseCase, GetMessagesUseCase {

    @EnableLogger
    private Logger logger;

    private final MessageRepositoryPort messageRepository;
    private final ThreadRepositoryPort threadRepository;

    @Override
    @Transactional
    public void sendMessage(SendMessageCommand command) {
        ensureThreadExists(command.threadId());

        Message message = Message.builder()
                .id(UUID.randomUUID())
                .threadId(command.threadId())
                .senderId(command.senderId())
                .text(command.text())
                .file(false)
                .systemMessage(false)
                .timestamp(Instant.now())
                .build();

        messageRepository.save(message);

        // TODO (Epic 2.2): Publish "New Message Saved" event to internal broker
        // for REST-to-WebSocket fanout across all server nodes.
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> getMessages(UUID threadId, Pageable pageable) {
        return messageRepository.findByThreadId(threadId, pageable);
    }

    /**
     * Idempotent lazy Thread creation.
     * <p>
     * Uses {@code existsById} (a cheap COUNT/EXISTS SQL) to avoid loading the full entity.
     * If the thread does not exist, it is created. If a concurrent transaction creates
     * the same thread between the check and the insert, the resulting
     * {@link DataIntegrityViolationException} is caught and safely ignored.
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
