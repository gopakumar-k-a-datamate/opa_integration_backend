package org.datamate.collaboration.chat.application.port.out;

import org.datamate.collaboration.chat.domain.model.Thread;
import java.util.Optional;
import java.util.UUID;

/**
 * Outgoing Port. Defines what the application needs from external systems (like the DB).
 * Dependency Inversion ensures the Domain doesn't know about JPA.
 */
public interface ThreadRepositoryPort {
    Thread save(Thread thread);
    Optional<Thread> findById(UUID id);
}
