package org.datamate.collaboration.chat.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.dto.MessageDto;
import org.datamate.collaboration.chat.application.dto.SendMessageRequest;
import org.datamate.collaboration.chat.application.port.in.GetMessagesUseCase;
import org.datamate.collaboration.chat.application.port.in.SendMessageUseCase;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PaginatedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
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
    @ResponseStatus(HttpStatus.CREATED)
    public void sendMessage(
            @PathVariable UUID threadId,
            @Valid @RequestBody SendMessageRequest request,
            Principal principal) {

        sendMessageUseCase.sendMessage(threadId, principal.getName(), request);
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
