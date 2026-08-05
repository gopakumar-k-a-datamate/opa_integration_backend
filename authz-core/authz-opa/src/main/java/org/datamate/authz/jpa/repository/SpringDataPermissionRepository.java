package org.datamate.authz.jpa.repository;

import org.datamate.authz.jpa.entity.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataPermissionRepository extends JpaRepository<PermissionJpaEntity, Long> {

    Optional<PermissionJpaEntity> findByCodeAndDeletedAtIsNull(String code);

    List<PermissionJpaEntity> findAllByDeletedAtIsNull();

    Optional<PermissionJpaEntity> findByResourceIdAndActionAndDeletedAtIsNull(
            Long resourceId, String action);
}

