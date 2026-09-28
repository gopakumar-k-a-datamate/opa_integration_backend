package org.datamate.collaboration.chat.adapter.in.rest;

import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.dto.MessageDto;
import org.datamate.collaboration.chat.application.dto.SendMessageRequest;
import org.datamate.collaboration.chat.application.port.in.GetMessagesUseCase;
import org.datamate.collaboration.chat.application.port.in.SendMessageUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;

/**
 * Incoming REST Adapter for the Chat domain.
 * <p>
 * Exposes the messaging API contracts defined in the Architecture Specification (Section 4):
 * <ul>
 *   <li>{@code POST /api/threads/{threadId}/messages} - Send a new message (supports JSON or multipart attachment)</li>
 *   <li>{@code GET  /api/threads/{threadId}/messages} - Fetch paginated chat history</li>
 * </ul>
 * <p>
 * Follows the RMS multi-part attachment pattern where text and files can be sent in a single request.
 */
@RestController
@RequestMapping("/api/threads/{threadId}/messages")
@RequiredArgsConstructor
public class ChatController {

    private final SendMessageUseCase sendMessageUseCase;
    private final GetMessagesUseCase getMessagesUseCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void sendMessage(
            @PathVariable UUID threadId,
            @Valid @RequestBody SendMessageRequest request,
            Principal principal) {

        sendMessageUseCase.sendMessage(threadId, principal.getName(), request);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void sendMessageWithAttachment(
            @PathVariable UUID threadId,
            @RequestParam(value = "text", required = false) String text,
            @RequestPart(value = "file", required = false) MultipartFile file,
            // TODO (Epic 3.1): Extract senderId from Security Principal (Ticket JWT)
            @RequestHeader("X-Sender-Id") String senderId) {

        sendMessageUseCase.sendMessageWithAttachment(threadId, senderId, text, file);
    }

    @GetMapping
    public PaginatedResponse<MessageDto> getMessages(
            @PathVariable UUID threadId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageQuery query = new PageQuery(page, size);
        Paged<MessageDto> paged = getMessagesUseCase.getMessages(threadId, query);
        return PaginatedResponse.of(paged);
    }
}