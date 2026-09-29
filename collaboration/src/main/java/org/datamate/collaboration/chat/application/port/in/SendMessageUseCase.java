package org.datamate.collaboration.chat.application.port.in;

import org.datamate.collaboration.chat.application.dto.SendMessageRequest;

import java.util.UUID;

/**
 * Inbound Use Case Port for sending messages in a thread.
 * Clean Architecture compliant: no web/HTTP framework dependencies.
 */
public interface SendMessageUseCase {
    void sendMessage(UUID threadId, String senderId, SendMessageRequest request);
}