package org.datamate.collaboration.chat.domain.model;

import java.util.UUID;

/**
 * Pure Domain Entity. No Spring or JPA annotations allowed here.
 */
public class Thread {
    private UUID id;

    public Thread(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
