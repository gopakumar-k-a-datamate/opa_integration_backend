package org.datamate.identity.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
