package org.datamate.collaboration.chat.application.usecase;

import org.datamate.collaboration.chat.application.dto.MessageDto;
import org.datamate.collaboration.chat.application.mapper.MessageMapper;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMessagesService")
class GetMessagesServiceTest {

    @Mock
    private MessageRepositoryPort messageRepository;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private GetMessagesService getMessagesService;

    private UUID threadId;

    @BeforeEach
    void setUp() {
        threadId = UUID.randomUUID();
    }

    @Test
    @DisplayName("should return paginated mapped message DTOs")
    void shouldReturnPaginatedMessages() {
        Pageable pageable = PageRequest.of(0, 20);
        String senderId = "user-123";
        Message message = Message.restore(
                UUID.randomUUID(),
                threadId,
                senderId,
                "Hello",
                false,
                false,
                null,
                Instant.now()
        );
        Page<Message> expectedPage = new PageImpl<>(List.of(message), pageable, 1);
        MessageDto expectedDto = new MessageDto(message.getId(), message.getSenderId(), message.getText(), message.isFile(), message.isSystemMessage(), message.getAttachmentId(), message.getTimestamp());

        when(messageRepository.findByThreadId(threadId, pageable)).thenReturn(expectedPage);
        when(messageMapper.toDto(message)).thenReturn(expectedDto);

        Page<MessageDto> result = getMessagesService.getMessages(threadId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).senderId()).isEqualTo(senderId);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("should return empty page when no messages exist")
    void shouldReturnEmptyPage_WhenNoMessagesExist() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Message> emptyPage = Page.empty(pageable);

        when(messageRepository.findByThreadId(threadId, pageable)).thenReturn(emptyPage);

        Page<MessageDto> result = getMessagesService.getMessages(threadId, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
