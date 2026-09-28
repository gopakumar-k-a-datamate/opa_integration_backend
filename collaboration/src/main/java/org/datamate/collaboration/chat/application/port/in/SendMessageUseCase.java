package org.datamate.collaboration.chat.application.port.in;

import org.datamate.collaboration.chat.application.dto.SendMessageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface SendMessageUseCase {
    void sendMessage(UUID threadId, String senderId, SendMessageRequest request);
    void sendMessageWithAttachment(UUID threadId, String senderId, String text, MultipartFile file);
}