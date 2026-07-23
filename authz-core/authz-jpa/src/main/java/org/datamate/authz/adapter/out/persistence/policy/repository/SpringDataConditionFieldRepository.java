package org.datamate.authz.adapter.out.persistence.policy.repository;

import org.datamate.authz.adapter.out.persistence.policy.entity.ConditionFieldJpaEntity;
import org.datamate.authz.domain.model.policy.enumtype.FieldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataConditionFieldRepository
        extends JpaRepository<ConditionFieldJpaEntity, Long> {

    /** Active-only fields (for Condition Builder UI). */
    List<ConditionFieldJpaEntity> findByPermissionIdAndStatusAndDeletedAtIsNull(
            Long permissionId, FieldStatus status);

    /** All fields including DEPRECATED (for diff-sync). */
    List<ConditionFieldJpaEntity> findByPermissionIdAndDeletedAtIsNull(Long permissionId);

    Optional<ConditionFieldJpaEntity> findByPermissionIdAndFieldNameAndDeletedAtIsNull(
            Long permissionId, String fieldName);

    List<ConditionFieldJpaEntity> findByStatusAndDeletedAtIsNull(FieldStatus status);
}

