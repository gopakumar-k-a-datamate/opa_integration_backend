package org.datamate.authz.dto.policy;

import org.datamate.authz.model.policy.enumtype.SubjectType;

/**
 * Criteria object encapsulating policy search parameters.
 *
 * @param subjectType Subject type (ROLE or USER)
 * @param subjectId   Subject identifier
 * @param namespace   Module namespace
 * @param search      Optional search keyword (permissionCode, resourceName, action)
 */
public record PolicySearchCriteria(
        SubjectType subjectType,
        String subjectId,
        String namespace,
        String search
) {}
