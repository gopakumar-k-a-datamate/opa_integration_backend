package org.datamate.authz.adapter.out.persistence.policy.repository;

import org.datamate.authz.adapter.out.persistence.policy.entity.PolicyJpaEntity;
import org.datamate.authz.domain.model.policy.enumtype.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataPolicyRepository extends JpaRepository<PolicyJpaEntity, Long> {

    /** All enabled non-deleted policies — for the compiler. */
    List<PolicyJpaEntity> findAllByEnabledTrueAndDeletedAtIsNull();

    /** Policies for a specific subject — for the Admin UI grid. */
    List<PolicyJpaEntity> findBySubjectTypeAndSubjectIdAndDeletedAtIsNull(
            SubjectType subjectType, String subjectId);

    Optional<PolicyJpaEntity> findByPermissionIdAndSubjectTypeAndSubjectIdAndDeletedAtIsNull(
            Long permissionId, SubjectType subjectType, String subjectId);

    /**
     * Finds enabled policies that reference a field name inside their expression_json.
     * Uses a LIKE search on the JSON text column — sufficient for field name matching.
     */
    @Query("SELECT p FROM PolicyJpaEntity p " +
           "WHERE p.permissionId = :permissionId " +
           "AND p.enabled = true " +
           "AND p.deletedAt IS NULL " +
           "AND CAST(p.expressionJson AS text) LIKE %:fieldName%")
    List<PolicyJpaEntity> findEnabledReferencingField(
            @Param("permissionId") Long permissionId,
            @Param("fieldName") String fieldName);
}

