package org.datamate.identity.domain.event;

import java.time.Instant;
import java.util.UUID;
import com.datamate.bedrock.framework.common.ddd.event.VersionedDomainEvent;

public record UserCreatedEvent(
        String eventId,
        UUID aggregateId,
        Long domainVersion,
        String schemaVersion,
        Instant occurredOn,

        String userName,
        String email,
        String phoneNumber,
        String firstName,
        String lastName,
        String createdBy
) implements VersionedDomainEvent<UUID> {
    public static final String SCHEMA_VERSION = "1.0";

    public UserCreatedEvent(
            UUID aggregateId, Long domainVersion, String userName, String email,
            String phoneNumber, String firstName, String lastName, String createdBy
    ) {
        this(
                UUID.randomUUID().toString(), aggregateId, domainVersion, SCHEMA_VERSION, Instant.now(),
                userName, email, phoneNumber, firstName, lastName, createdBy
        );
    }
}
