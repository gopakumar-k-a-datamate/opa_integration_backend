package org.datamate.collaboration.chat.application.usecase;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.collaboration.chat.application.dto.SendMessageRequest;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Message;
import org.datamate.collaboration.chat.domain.model.Thread;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendMessageService")
class SendMessageServiceTest {

    @Mock
    private MessageRepositoryPort messageRepository;

    @Mock
    private ThreadRepositoryPort threadRepository;

    @Mock
    private Logger logger;

    @InjectMocks
    private SendMessageService sendMessageService;

    private UUID threadId;
    private String senderId;
    private String text;
    private SendMessageRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sendMessageService, "logger", logger);
        threadId = UUID.randomUUID();
        senderId = "user-john";
        text = "Hello, World!";
        request = new SendMessageRequest(text);
    }

    @Test
    @DisplayName("should lazily create thread when it does not exist")
    void shouldCreateThread_WhenThreadDoesNotExist() {
        when(threadRepository.existsById(threadId)).thenReturn(false);

        sendMessageService.sendMessage(threadId, senderId, request);

        ArgumentCaptor<Thread> threadCaptor = ArgumentCaptor.forClass(Thread.class);
        verify(threadRepository).save(threadCaptor.capture());
        assertThat(threadCaptor.getValue().getId()).isEqualTo(threadId);
    }

    @Test
    @DisplayName("should skip thread creation when thread already exists (idempotent)")
    void shouldNotCreateThread_WhenThreadAlreadyExists() {
        when(threadRepository.existsById(threadId)).thenReturn(true);

        sendMessageService.sendMessage(threadId, senderId, request);

        verify(threadRepository, never()).save(any(Thread.class));
    }

    @Test
    @DisplayName("should handle concurrent thread creation gracefully")
    void shouldHandleConcurrentThreadCreation() {
        when(threadRepository.existsById(threadId)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("Duplicate key"))
                .when(threadRepository).save(any(Thread.class));

        sendMessageService.sendMessage(threadId, senderId, request);

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("should persist message with correct fields")
    void shouldPersistMessage_WithCorrectFields() {
        when(threadRepository.existsById(threadId)).thenReturn(true);

        sendMessageService.sendMessage(threadId, senderId, request);

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

        sendMessageService.sendMessage(threadId, senderId, request);
        sendMessageService.sendMessage(threadId, senderId, request);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, times(2)).save(captor.capture());

        List<Message> savedMessages = captor.getAllValues();
        assertThat(savedMessages.get(0).getId())
                .isNotEqualTo(savedMessages.get(1).getId());
    }
}
