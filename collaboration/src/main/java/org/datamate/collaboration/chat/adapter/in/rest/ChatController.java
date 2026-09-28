package org.datamate.collaboration.chat.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.dto.MessageDto;
import org.datamate.collaboration.chat.application.dto.SendMessageRequest;
import org.datamate.collaboration.chat.application.port.in.GetMessagesUseCase;
import org.datamate.collaboration.chat.application.port.in.SendMessageUseCase;
import org.datamate.collaboration.chat.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Incoming REST Adapter for the Chat domain.
 * <p>
 * Exposes the messaging API contracts defined in the Architecture Specification (Section 4):
 * <ul>
 *   <li>{@code POST /api/threads/{threadId}/messages} — Send a new message</li>
 *   <li>{@code GET  /api/threads/{threadId}/messages} — Fetch paginated chat history</li>
 * </ul>
 * <p>
 * Depends only on Application Ports ({@link SendMessageUseCase}, {@link GetMessagesUseCase}),
 * never on domain services or persistence adapters directly.
 * <p>
 * <strong>TODO (Epic 3.1):</strong> Replace temporary {@code X-Sender-Id} header
 * with the authenticated security principal from the Ticket JWT.
 */
@RestController
@RequestMapping("/api/threads/{threadId}/messages")
@RequiredArgsConstructor
public class ChatController {

    private final SendMessageUseCase sendMessageUseCase;
    private final GetMessagesUseCase getMessagesUseCase;

    @PostMapping
    public ResponseEntity<Void> sendMessage(
            @PathVariable UUID threadId,
            @Valid @RequestBody SendMessageRequest request,
            // TODO (Epic 3.1): Extract senderId from Security Principal (Ticket JWT)
            @RequestHeader("X-Sender-Id") String senderId) {

        sendMessageUseCase.sendMessage(
                new SendMessageUseCase.SendMessageCommand(threadId, senderId, request.text())
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<Page<MessageDto>> getMessages(
            @PathVariable UUID threadId,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<Message> messages = getMessagesUseCase.getMessages(threadId, pageable);
        return ResponseEntity.ok(messages.map(this::toResponse));
    }

    private MessageDto toResponse(Message message) {
        return new MessageDto(
                message.getId(),
                message.getSenderId(),
                message.getText(),
                message.isFile(),
                message.isSystemMessage(),
                message.getAttachmentId(),
                message.getTimestamp()
        );
    }
}
