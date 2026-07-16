package org.datamate.authz.application.port.out.policy;

import org.datamate.authz.domain.model.policy.enumtype.FieldType;
import org.datamate.authz.domain.model.policy.entity.ConditionField;
import org.datamate.authz.domain.model.policy.enumtype.FieldStatus;

import java.util.List;
import java.util.Optional;

/** Persistence operations for {@code authz_condition_field}. */
public interface ConditionFieldPersistencePort {

    /** Insert or update a field identified by {@code (permissionId, fieldName)}. */
    ConditionField upsert(Long id, Long permissionId, String fieldName, FieldType fieldType,
                               String displayName, List<String> allowedValues, String optionsEndpoint);

    /** Returns only ACTIVE, non-deleted fields for the given permission. */
    List<ConditionField> findActiveByPermissionId(Long permissionId);

    /** Returns ALL fields (including DEPRECATED) for diff-sync comparison. */
    List<ConditionField> findAllByPermissionId(Long permissionId);

    Optional<ConditionField> findByPermissionIdAndFieldName(Long permissionId, String fieldName);

    /** Set field status to DEPRECATED (does not soft-delete). */
    void markDeprecated(Long id);

    /** Soft-delete the field (sets deleted_at). */
    void softDelete(Long id);
}


