package org.datamate.collaboration.chat.application.usecase;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.dto.MessageDto;
import org.datamate.collaboration.chat.application.mapper.MessageMapper;
import org.datamate.collaboration.chat.application.port.in.GetMessagesUseCase;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application Service for retrieving messages.
 * <p>
 * Implements {@link GetMessagesUseCase}. This handles the query side of messaging,
 * mapping domain models to DTOs for the presentation layer.
 */
@Service
@RequiredArgsConstructor
public class GetMessagesService implements GetMessagesUseCase {

    private final MessageRepositoryPort messageRepository;
    private final MessageMapper messageMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDto> getMessages(UUID threadId, Pageable pageable) {
        return messageRepository.findByThreadId(threadId, pageable)
                .map(messageMapper::toDto);
    }
}
