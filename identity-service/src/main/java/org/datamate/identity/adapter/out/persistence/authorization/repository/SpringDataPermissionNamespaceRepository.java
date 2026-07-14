package org.datamate.identity.adapter.out.persistence.authorization.repository;

import java.util.UUID;
import org.datamate.identity.adapter.out.persistence.authorization.entity.PermissionNamespaceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataPermissionNamespaceRepository extends JpaRepository<PermissionNamespaceJpaEntity, UUID> {
}
