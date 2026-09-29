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

import java.security.Principal;
import java.util.UUID;

/**
 * Incoming REST Adapter for the Chat domain.
 * <p>
 * Exposes the messaging API contracts:
 * <ul>
 *   <li>{@code POST /api/threads/{threadId}/messages} - Send a new message (JSON payload supporting text and pre-uploaded attachment URLs)</li>
 *   <li>{@code GET  /api/threads/{threadId}/messages} - Fetch paginated chat history</li>
 * </ul>
 * <p>
 * Follows the RMS pattern where the frontend uploads files to MinIO first, obtains the URLs,
 * and sends them in the JSON body.
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

        String senderId = (principal != null && principal.getName() != null) ? principal.getName() : "Anonymous";
        sendMessageUseCase.sendMessage(threadId, senderId, request);
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