package org.datamate.collaboration.chat.adapter.out.persistence.attachment.repository;

import org.datamate.collaboration.chat.adapter.out.persistence.attachment.entity.AttachmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link AttachmentJpaEntity}.
 */
public interface AttachmentJpaRepository extends JpaRepository<AttachmentJpaEntity, UUID> {
    java.util.Optional<AttachmentJpaEntity> findByUploadUrl(String uploadUrl);
}

