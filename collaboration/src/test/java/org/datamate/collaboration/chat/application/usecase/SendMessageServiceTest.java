package org.datamate.collaboration.chat.application.usecase;

import org.datamate.collaboration.chat.application.dto.MessageDto;
import org.datamate.collaboration.chat.application.dto.SendMessageCommand;
import org.datamate.collaboration.chat.application.mapper.MessageMapper;
import org.datamate.collaboration.chat.application.port.out.AttachmentRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.MessageBroadcastPort;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Attachment;
import org.datamate.collaboration.chat.domain.model.Message;
import org.datamate.collaboration.chat.domain.model.Thread;
import org.datamate.collaboration.exception.ApplicationValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendMessageService Unit Tests")
class SendMessageServiceTest {

    @Mock
    private MessageRepositoryPort messageRepository;

    @Mock
    private ThreadRepositoryPort threadRepository;

    @Mock
    private AttachmentRepositoryPort attachmentRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private MessageBroadcastPort messageBroadcastPort;

    @InjectMocks
    private SendMessageService sendMessageService;

    @Test
    @DisplayName("Should successfully send a text message and broadcast it")
    void shouldSendTextMessageAndBroadcast() {
        UUID threadId = UUID.randomUUID();
        String senderId = "user1";
        SendMessageCommand command = new SendMessageCommand("Hello World", (UUID) null);

        Message savedMessage = Message.create(threadId, senderId, "Hello World", false, false, null);
        MessageDto dto = new MessageDto(java.util.UUID.randomUUID(), "user1", "text", false, false, java.util.List.of(), java.time.Instant.now());

        when(threadRepository.existsById(threadId)).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageMapper.toDto(savedMessage, null)).thenReturn(dto);

        sendMessageService.sendMessage(threadId, senderId, command);

        verify(messageRepository).save(any(Message.class));
        verify(messageBroadcastPort).broadcastMessage(threadId, dto);
        verify(attachmentRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw exception if command is missing text and attachmentId")
    void shouldThrowExceptionWhenCommandIsInvalid() {
        UUID threadId = UUID.randomUUID();
        String senderId = "user1";
        SendMessageCommand command = new SendMessageCommand("", (UUID) null);

        assertThatThrownBy(() -> sendMessageService.sendMessage(threadId, senderId, command))
                .isInstanceOf(ApplicationValidationException.class);
    }
    
    @Test
    @DisplayName("Should send message with attachment and broadcast")
    void shouldSendMessageWithAttachment() {
        UUID threadId = UUID.randomUUID();
        String senderId = "user1";
        UUID attachmentId = UUID.randomUUID();
        SendMessageCommand command = new SendMessageCommand("Here is a file", attachmentId);

        Message savedMessage = Message.create(threadId, senderId, "Here is a file", true, false, attachmentId);
        Attachment attachment = Attachment.create("test.png", "image/png", 1024L, "url", "preview");
        MessageDto dto = new MessageDto(java.util.UUID.randomUUID(), "user1", "text", false, false, java.util.List.of(), java.time.Instant.now());

        when(threadRepository.existsById(threadId)).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(messageMapper.toDto(savedMessage, attachment)).thenReturn(dto);

        sendMessageService.sendMessage(threadId, senderId, command);

        verify(messageRepository).save(any(Message.class));
        verify(messageBroadcastPort).broadcastMessage(threadId, dto);
    }
}

