package org.datamate.collaboration.chat.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.port.in.CreateThreadUseCase;
import org.springframework.web.bind.annotation.RestController;

/**
 * Incoming Adapter. Depends on Application port, never on Domain directly if possible.
 */
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final CreateThreadUseCase useCase;
}
