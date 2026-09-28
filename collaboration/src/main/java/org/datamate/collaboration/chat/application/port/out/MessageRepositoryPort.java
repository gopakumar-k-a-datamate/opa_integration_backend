package org.datamate.collaboration.chat.application.port.out;

import org.datamate.collaboration.chat.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface MessageRepositoryPort {
    Message save(Message message);
    Page<Message> findByThreadId(UUID threadId, Pageable pageable);
}
