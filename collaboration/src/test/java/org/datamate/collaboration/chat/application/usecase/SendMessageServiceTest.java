package org.datamate.collaboration.chat.application.usecase;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.datamate.bedrock.framework.storage.application.port.StorageService;
import com.datamate.bedrock.framework.storage.domain.model.StorageObject;
import org.datamate.collaboration.chat.application.dto.SendMessageRequest;
import org.datamate.collaboration.chat.application.port.out.AttachmentRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Attachment;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendMessageService")
class SendMessageServiceTest {

    @Mock
    private MessageRepositoryPort messageRepository;

    @Mock
    private ThreadRepositoryPort threadRepository;

    @Mock
    private AttachmentRepositoryPort attachmentRepository;

    @Mock
    private StorageService storageService;

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
        ReflectionTestUtils.setField(sendMessageService, "defaultBucket", "chat-attachments");
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
    @DisplayName("should persist message with attachment when file is provided")
    void shouldPersistMessage_WithAttachment_WhenFileProvided() {
        when(threadRepository.existsById(threadId)).thenReturn(true);
        when(storageService.bucketExists("chat-attachments")).thenReturn(true);
        when(storageService.upload(eq("chat-attachments"), anyString(), any(InputStream.class), anyString(), anyLong()))
                .thenReturn(StorageObject.builder()
                        .bucketName("chat-attachments")
                        .objectKey("threads/" + threadId + "/doc.pdf")
                        .size(100L)
                        .contentType("application/pdf")
                        .build());

        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "dummy pdf content".getBytes()
        );

        sendMessageService.sendMessageWithAttachment(threadId, senderId, "Attached PDF", file);

        verify(attachmentRepository).save(any(Attachment.class));

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());

        Message saved = messageCaptor.getValue();
        assertThat(saved.isFile()).isTrue();
        assertThat(saved.getAttachmentId()).isNotNull();
        assertThat(saved.getText()).isEqualTo("Attached PDF");
    }

    @Test
    @DisplayName("should reject blocked file extension")
    void shouldRejectBlockedFileExtension() {
        MockMultipartFile blockedFile = new MockMultipartFile(
                "file", "malicious.exe", "application/octet-stream", "bad content".getBytes()
        );

        assertThatThrownBy(() -> sendMessageService.sendMessageWithAttachment(threadId, senderId, null, blockedFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File type not allowed");
    }

    @Test
    @DisplayName("should reject when both text and file are empty")
    void shouldReject_WhenBothTextAndFileEmpty() {
        assertThatThrownBy(() -> sendMessageService.sendMessageWithAttachment(threadId, senderId, "  ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Message text or file attachment must be provided");
    }
}