package org.datamate.identity.shared.event.role;

import java.time.Instant;
import java.util.UUID;
import org.datamate.identity.shared.model.RoleStatus;
import com.datamate.bedrock.framework.common.ddd.event.VersionedDomainEvent;

public record RoleCreatedEvent(
        String eventId,
        Long aggregateId,
        Long domainVersion,
        String schemaVersion,
        Instant occurredOn,

        String name,
        String description,
        RoleStatus status,
        String createdBy
) implements VersionedDomainEvent<Long> {
    public static final String SCHEMA_VERSION = "1.0";

    public RoleCreatedEvent(
            Long aggregateId, Long domainVersion, String name, String description,
            RoleStatus status, String createdBy
    ) {
        this(
                UUID.randomUUID().toString(), aggregateId, domainVersion, SCHEMA_VERSION, Instant.now(),
                name, description, status, createdBy
        );
    }
}
