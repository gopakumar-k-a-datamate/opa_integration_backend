package org.datamate.collaboration.chat.adapter.out.persistence.thread.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * JPA Entity for the {@code chat_threads} table.
 * <p>
 * Used exclusively by the persistence adapter layer.
 * Must never leak into the domain or application layers.
 */
@Entity
@Table(name = "chat_threads")
public class ThreadJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    public ThreadJpaEntity() {}
    public ThreadJpaEntity(UUID id) { this.id = id; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
}