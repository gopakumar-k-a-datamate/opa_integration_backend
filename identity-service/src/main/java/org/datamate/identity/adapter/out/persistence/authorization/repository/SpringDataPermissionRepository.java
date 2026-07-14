package org.datamate.identity.adapter.out.persistence.authorization.repository;

import java.util.List;
import java.util.UUID;
import org.datamate.identity.adapter.out.persistence.authorization.entity.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataPermissionRepository extends JpaRepository<PermissionJpaEntity, UUID> {
    List<PermissionJpaEntity> findByResourceId(UUID resourceId);
}
