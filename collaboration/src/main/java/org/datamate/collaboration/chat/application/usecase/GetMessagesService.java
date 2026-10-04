package org.datamate.collaboration.chat.application.usecase;

import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.Paged;
import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.dto.GetMessagesQuery;
import org.datamate.collaboration.chat.application.dto.MessageDto;
import org.datamate.collaboration.chat.application.mapper.MessageMapper;
import org.datamate.collaboration.chat.application.port.in.GetMessagesUseCase;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
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
    public Paged<MessageDto> getMessages(UUID threadId, PageQuery query) {
        return messageRepository.findByThreadId(threadId, query)
                .map(messageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Paged<MessageDto> getMessages(UUID threadId, GetMessagesQuery query) {
        PageQuery pageQuery = query != null ? query.toPageQuery() : new PageQuery(1, 10);
        return getMessages(threadId, pageQuery);
    }
}