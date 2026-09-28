package org.datamate.collaboration.chat.application.usecase;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.collaboration.chat.application.port.in.SendMessageUseCase.SendMessageCommand;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Message;
import org.datamate.collaboration.chat.domain.model.Thread;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService")
class MessageServiceTest {

    @Mock
    private MessageRepositoryPort messageRepository;

    @Mock
    private ThreadRepositoryPort threadRepository;

    @Mock
    private Logger logger;

    @InjectMocks
    private MessageService messageService;

    private UUID threadId;
    private String senderId;
    private String text;
    private SendMessageCommand command;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(messageService, "logger", logger);
        threadId = UUID.randomUUID();
        senderId = "user-john";
        text = "Hello, World!";
        command = new SendMessageCommand(threadId, senderId, text);
    }

    @Nested
    @DisplayName("sendMessage")
    class SendMessageTests {

        @Test
        @DisplayName("should lazily create thread when it does not exist")
        void shouldCreateThread_WhenThreadDoesNotExist() {
            when(threadRepository.existsById(threadId)).thenReturn(false);

            messageService.sendMessage(command);

            ArgumentCaptor<Thread> threadCaptor = ArgumentCaptor.forClass(Thread.class);
            verify(threadRepository).save(threadCaptor.capture());
            assertThat(threadCaptor.getValue().getId()).isEqualTo(threadId);
        }

        @Test
        @DisplayName("should skip thread creation when thread already exists (idempotent)")
        void shouldNotCreateThread_WhenThreadAlreadyExists() {
            when(threadRepository.existsById(threadId)).thenReturn(true);

            messageService.sendMessage(command);

            verify(threadRepository, never()).save(any(Thread.class));
        }

        @Test
        @DisplayName("should handle concurrent thread creation gracefully")
        void shouldHandleConcurrentThreadCreation() {
            when(threadRepository.existsById(threadId)).thenReturn(false);
            doThrow(new DataIntegrityViolationException("Duplicate key"))
                    .when(threadRepository).save(any(Thread.class));

            messageService.sendMessage(command);

            verify(messageRepository).save(any(Message.class));
        }

        @Test
        @DisplayName("should persist message with correct fields")
        void shouldPersistMessage_WithCorrectFields() {
            when(threadRepository.existsById(threadId)).thenReturn(true);

            messageService.sendMessage(command);

            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(messageRepository).save(messageCaptor.capture());

            Message saved = messageCaptor.getValue();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getThreadId()).isEqualTo(threadId);
            assertThat(saved.getSenderId()).isEqualTo(senderId);
            assertThat(saved.getText()).isEqualTo(text);
            assertThat(saved.isFile()).isFalse();
            assertThat(saved.isSystemMessage()).isFalse();
            assertThat(saved.getAttachmentId()).isNull();
            assertThat(saved.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should generate unique message ID for each invocation")
        void shouldGenerateUniqueMessageId() {
            when(threadRepository.existsById(threadId)).thenReturn(true);

            messageService.sendMessage(command);
            messageService.sendMessage(command);

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(messageRepository, times(2)).save(captor.capture());

            List<Message> savedMessages = captor.getAllValues();
            assertThat(savedMessages.get(0).getId())
                    .isNotEqualTo(savedMessages.get(1).getId());
        }
    }

    @Nested
    @DisplayName("getMessages")
    class GetMessagesTests {

        @Test
        @DisplayName("should return paginated messages for a thread")
        void shouldReturnPaginatedMessages() {
            Pageable pageable = PageRequest.of(0, 20);
            Message message = Message.builder()
                    .id(UUID.randomUUID())
                    .threadId(threadId)
                    .senderId(senderId)
                    .text(text)
                    .file(false)
                    .systemMessage(false)
                    .timestamp(Instant.now())
                    .build();
            Page<Message> expectedPage = new PageImpl<>(List.of(message), pageable, 1);

            when(messageRepository.findByThreadId(threadId, pageable)).thenReturn(expectedPage);

            Page<Message> result = messageService.getMessages(threadId, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getThreadId()).isEqualTo(threadId);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return empty page when no messages exist")
        void shouldReturnEmptyPage_WhenNoMessagesExist() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Message> emptyPage = Page.empty(pageable);

            when(messageRepository.findByThreadId(threadId, pageable)).thenReturn(emptyPage);

            Page<Message> result = messageService.getMessages(threadId, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}
