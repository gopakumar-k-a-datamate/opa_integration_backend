package org.datamate.collaboration.chat.application.port.in;

import org.datamate.collaboration.chat.domain.model.Thread;
import java.util.UUID;

/**
 * Incoming Port. Defines use cases.
 */
public interface CreateThreadUseCase {
    Thread create(UUID id);
}
