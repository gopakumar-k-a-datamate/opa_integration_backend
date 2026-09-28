package org.datamate.collaboration.chat.application.port.out;

import org.datamate.collaboration.chat.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Outgoing Port for Message persistence.
 * <p>
 * Defines what the application layer needs from the persistence infrastructure
 * for managing Message records. Queries use raw {@code threadId} (UUID)
 * instead of entity joins to avoid N+1 query problems.
 */
public interface MessageRepositoryPort {

    Message save(Message message);

    Page<Message> findByThreadId(UUID threadId, Pageable pageable);
}
