package org.datamate.collaboration.chat.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA Entity used exclusively by the Persistence Adapter.
 */
@Entity
@Table(name = "chat_threads")
@Getter
@Setter
public class ThreadJpaEntity {
    @Id
    private UUID id;
}
