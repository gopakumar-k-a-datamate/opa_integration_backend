package org.datamate.collaboration.chat.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, UUID> {
    Page<MessageJpaEntity> findByThreadIdOrderByTimestampDesc(UUID threadId, Pageable pageable);
}
