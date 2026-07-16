package org.datamate.authz.adapter.out.persistence.policy.repository;

import org.datamate.authz.adapter.out.persistence.policy.entity.ResourceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataResourceRepository extends JpaRepository<ResourceJpaEntity, UUID> {

    List<ResourceJpaEntity> findAllByDeletedAtIsNull();

    Optional<ResourceJpaEntity> findByNamespaceAndNameAndDeletedAtIsNull(
            String namespace, String name);
}

