package org.datamate.authz.application.dto.policy;

import org.datamate.authz.domain.model.policy.enumtype.SubjectType;

import java.util.List;

/**
 * Request body for {@code PUT /internal/authz/policies}.
 * Represents the complete desired state for a subject within this module.
 *
 * <p>The service performs a full-state sync:
 * <ul>
 *   <li>Items with {@code isDeleted=true} are soft-deleted.</li>
 *   <li>Items present in the payload are upserted.</li>
 *   <li>Existing DB policies absent from the payload are soft-deleted.</li>
 * </ul>
 * </p>
 */
public record SavePoliciesRequest(
        SubjectType subjectType,
        String subjectId,
        List<PolicyItemDto> policies
) {}


