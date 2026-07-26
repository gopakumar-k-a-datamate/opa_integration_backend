package org.datamate.identity.domain.model;

import org.datamate.identity.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot {

    private Long domainVersion = 0L;

    private final List<DomainEvent> domainEvents;

    public AggregateRoot() {
        this.domainEvents = new ArrayList<>();
    }

    public AggregateRoot(Long domainVersion) {
        this();
        this.domainVersion = domainVersion != null ? domainVersion : 0L;
    }

    protected void registerEvent(DomainEvent event) {
        if (this.domainVersion == null) {
            this.domainVersion = 0L;
        }
        this.domainVersion++;
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public Long getDomainVersion() {
        return domainVersion;
    }

    public Long nextVersion() {
        return (this.domainVersion == null ? 0L : this.domainVersion) + 1;
    }

    public void setDomainVersion(Long domainVersion) {
        if (this.domainVersion != null && this.domainVersion > 0) return;
        this.domainVersion = domainVersion != null ? domainVersion : 0L;
    }
}
