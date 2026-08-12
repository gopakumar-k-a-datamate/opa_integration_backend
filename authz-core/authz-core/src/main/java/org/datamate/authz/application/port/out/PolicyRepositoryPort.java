package org.datamate.authz.application.port.out;

import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.model.policy.enumtype.SubjectType;

import java.util.List;

/** Persistence operations for {@code authz_policy}. */
public interface PolicyRepositoryPort {

    /** All active (non-deleted, enabled) policies — used by the compiler. */
    List<Policy> findAllEnabled();

    /** All active (non-deleted) policies, regardless of enabled status. */
    List<Policy> findAllActive();

    /** Active policies for a specific subject — used by the Admin UI grid. */
    List<Policy> findBySubject(SubjectType subjectType, String subjectId);

    /** Active policies that reference a given field name inside their expression_json. */
    List<Policy> findEnabledReferencingField(Long permissionId, String fieldName);

    /** Insert or update a policy. */
    Policy upsert(Long id, Long permissionId, SubjectType subjectType, String subjectId,
                       PolicyEffect effect, String expressionJson, boolean enabled,
                       String disabledReason, boolean useCustomRego, String customRegoSnippet);

    /** Update the deprecated status of a policy. */
    void updateDeprecatedStatus(Long id, boolean deprecated);

    /** Soft-delete a policy (sets deleted_at and deleted_reason). */
    void softDelete(Long id, String reason);

    /** Set enabled=false and record the reason — used during field deprecation. */
    void autoDisable(Long id, String reason);
}



