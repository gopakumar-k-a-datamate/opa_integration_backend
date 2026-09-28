package org.datamate.collaboration.chat.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.port.in.GetMessagesUseCase;
import org.datamate.collaboration.chat.application.port.in.SendMessageUseCase;
import org.datamate.collaboration.chat.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Incoming Adapter. Depends on Application port, never on Domain directly if possible.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatController {
    
    private final SendMessageUseCase sendMessageUseCase;
    private final GetMessagesUseCase getMessagesUseCase;

    @PostMapping
    public ResponseEntity<Void> sendMessage(
            @RequestBody SendMessageRequest request,
            // TODO: Extract threadId and senderId from Security Principal (Epic 3.1)
            @RequestHeader("X-Thread-Id") UUID threadId,
            @RequestHeader("X-Sender-Id") String senderId) {
        
        sendMessageUseCase.sendMessage(new SendMessageUseCase.SendMessageCommand(
                threadId, senderId, request.text()
        ));
        
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<MessageResponse>> getMessages(
            Pageable pageable,
            // TODO: Extract threadId from Security Principal (Epic 3.1)
            @RequestHeader("X-Thread-Id") UUID threadId) {
            
        Page<Message> messages = getMessagesUseCase.getMessages(threadId, pageable);
        return ResponseEntity.ok(messages.map(this::toResponse));
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSenderId(),
                message.getText(),
                message.isFile(),
                message.isSystemMessage(),
                message.getAttachmentId(),
                message.getTimestamp().toString()
        );
    }

    public record SendMessageRequest(String text) {}
    public record MessageResponse(UUID id, String senderId, String text, boolean isFile, boolean isSystemMessage, UUID attachmentId, String timestamp) {}
}
