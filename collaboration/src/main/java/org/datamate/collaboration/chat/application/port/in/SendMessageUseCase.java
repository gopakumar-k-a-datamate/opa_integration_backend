package org.datamate.collaboration.chat.application.port.in;

import java.util.UUID;

public interface SendMessageUseCase {
    void sendMessage(SendMessageCommand command);

    record SendMessageCommand(UUID threadId, String senderId, String text) {}
}
