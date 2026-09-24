package org.datamate.collaboration.chat.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ThreadJpaRepository extends JpaRepository<ThreadJpaEntity, UUID> {
}
