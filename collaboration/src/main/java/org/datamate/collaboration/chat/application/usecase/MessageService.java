package org.datamate.collaboration.chat.application.usecase;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.port.in.GetMessagesUseCase;
import org.datamate.collaboration.chat.application.port.in.SendMessageUseCase;
import org.datamate.collaboration.chat.application.port.out.MessageRepositoryPort;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Message;
import org.datamate.collaboration.chat.domain.model.Thread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService implements SendMessageUseCase, GetMessagesUseCase {
    private final MessageRepositoryPort messageRepository;
    private final ThreadRepositoryPort threadRepository;

    @Override
    @Transactional
    public void sendMessage(SendMessageCommand command) {
        // Lazy Thread Creation
        if (threadRepository.findById(command.threadId()).isEmpty()) {
            threadRepository.save(new Thread(command.threadId()));
        }

        Message message = Message.createNew(
                command.threadId(),
                command.senderId(),
                command.text(),
                false
        );
        
        messageRepository.save(message);
        
        // Broadcast via broker or websocket logic will be added here in Epic 2
    }

    @Override
    public Page<Message> getMessages(UUID threadId, Pageable pageable) {
        return messageRepository.findByThreadId(threadId, pageable);
    }
}
