package org.datamate.authz.adapter.out.persistence.policy.repository;

import org.datamate.authz.adapter.out.persistence.policy.entity.ConditionFieldJpaEntity;
import org.datamate.authz.domain.model.policy.enumtype.FieldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataConditionFieldRepository
        extends JpaRepository<ConditionFieldJpaEntity, UUID> {

    /** Active-only fields (for Condition Builder UI). */
    List<ConditionFieldJpaEntity> findByPermissionIdAndStatusAndDeletedAtIsNull(
            UUID permissionId, FieldStatus status);

    /** All fields including DEPRECATED (for diff-sync). */
    List<ConditionFieldJpaEntity> findByPermissionIdAndDeletedAtIsNull(UUID permissionId);

    Optional<ConditionFieldJpaEntity> findByPermissionIdAndFieldNameAndDeletedAtIsNull(
            UUID permissionId, String fieldName);
}

