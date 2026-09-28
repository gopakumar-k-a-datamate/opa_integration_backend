package org.datamate.collaboration.chat.adapter.out.persistence.message.adapter;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.adapter.out.persistence.message.entity.MessageJpaEntity;
import org.datamate.collaboration.chat.adapter.out.persistence.message.repository.MessageJpaRepository;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Outgoing Adapter implementing {@link MessageRepositoryPort} using JPA.
 * <p>
 * Handles bidirectional mapping between the pure domain {@link Message}
 * and the JPA-managed {@link MessageJpaEntity}.
 */
@Component
@RequiredArgsConstructor
public class MessagePersistenceAdapter implements MessageRepositoryPort {

    private final MessageJpaRepository repository;

    @Override
    public Message save(Message message) {
        MessageJpaEntity entity = toEntity(message);
        repository.save(entity);
        return message;
    }

    @Override
    public Page<Message> findByThreadId(UUID threadId, Pageable pageable) {
        return repository.findByThreadIdOrderByTimestampDesc(threadId, pageable)
                .map(this::toDomain);
    }

    private MessageJpaEntity toEntity(Message message) {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.setId(message.getId());
        entity.setThreadId(message.getThreadId());
        entity.setSenderId(message.getSenderId());
        entity.setText(message.getText());
        entity.setFile(message.isFile());
        entity.setSystemMessage(message.isSystemMessage());
        entity.setAttachmentId(message.getAttachmentId());
        entity.setTimestamp(message.getTimestamp());
        return entity;
    }

    private Message toDomain(MessageJpaEntity entity) {
        return Message.builder()
                .id(entity.getId())
                .threadId(entity.getThreadId())
                .senderId(entity.getSenderId())
                .text(entity.getText())
                .file(entity.isFile())
                .systemMessage(entity.isSystemMessage())
                .attachmentId(entity.getAttachmentId())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
