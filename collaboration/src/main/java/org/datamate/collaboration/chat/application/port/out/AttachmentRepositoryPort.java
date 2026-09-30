package org.datamate.collaboration.chat.application.port.out;

import org.datamate.collaboration.chat.domain.model.Attachment;

import java.util.Optional;
import java.util.UUID;

/**
 * Outgoing Port for Attachment persistence.
 * <p>
 * Defines what the application layer needs from the persistence infrastructure
 * for managing Attachment records.
 */
public interface AttachmentRepositoryPort {

    Attachment save(Attachment attachment);

    Optional<Attachment> findById(UUID id);
}
