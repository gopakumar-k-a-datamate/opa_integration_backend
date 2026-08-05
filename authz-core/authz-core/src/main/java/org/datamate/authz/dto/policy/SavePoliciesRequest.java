package org.datamate.authz.dto.policy;

import org.datamate.authz.model.policy.enumtype.SubjectType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

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
        @NotNull(message = "subjectType is required") SubjectType subjectType,
        @NotBlank(message = "subjectId is required") String subjectId,
        @NotBlank(message = "namespace is required") String namespace,
        @NotNull(message = "policies array cannot be null") @Valid List<PolicyItemRequest> policies
) {}


