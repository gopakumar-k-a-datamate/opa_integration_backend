package org.datamate.collaboration.chat.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessagePersistenceAdapter implements MessageRepositoryPort {
    private final MessageJpaRepository repository;

    @Override
    public Message save(Message message) {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.setId(message.getId());
        entity.setThreadId(message.getThreadId());
        entity.setSenderId(message.getSenderId());
        entity.setText(message.getText());
        entity.setFile(message.isFile());
        entity.setSystemMessage(message.isSystemMessage());
        entity.setAttachmentId(message.getAttachmentId());
        entity.setTimestamp(message.getTimestamp());
        repository.save(entity);
        return message;
    }

    @Override
    public Page<Message> findByThreadId(UUID threadId, Pageable pageable) {
        return repository.findByThreadIdOrderByTimestampDesc(threadId, pageable)
                .map(entity -> Message.builder()
                        .id(entity.getId())
                        .threadId(entity.getThreadId())
                        .senderId(entity.getSenderId())
                        .text(entity.getText())
                        .isFile(entity.isFile())
                        .isSystemMessage(entity.isSystemMessage())
                        .attachmentId(entity.getAttachmentId())
                        .timestamp(entity.getTimestamp())
                        .build());
    }
}
