package org.datamate.identity.adapter.out.persistence.authorization.repository;

import java.util.List;
import java.util.UUID;
import org.datamate.identity.adapter.out.persistence.authorization.entity.ResourceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataResourceRepository extends JpaRepository<ResourceJpaEntity, UUID> {
    List<ResourceJpaEntity> findByNamespaceId(UUID namespaceId);
}
