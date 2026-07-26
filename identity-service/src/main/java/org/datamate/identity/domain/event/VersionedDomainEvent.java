package org.datamate.identity.domain.event;

public interface VersionedDomainEvent<ID> extends DomainEvent {
    ID aggregateId();
    Long domainVersion();
    String eventId();
    String schemaVersion();
}
