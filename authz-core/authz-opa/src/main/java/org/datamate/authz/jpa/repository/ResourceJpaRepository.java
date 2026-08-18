package org.datamate.authz.jpa.repository;

import org.datamate.authz.jpa.entity.ResourceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceJpaRepository extends JpaRepository<ResourceJpaEntity, Long> {

    List<ResourceJpaEntity> findAllByDeletedAtIsNull();

    Optional<ResourceJpaEntity> findByNamespaceAndNameAndDeletedAtIsNull(
            String namespace, String name);
}

