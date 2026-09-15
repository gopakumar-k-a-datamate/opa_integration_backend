package org.datamate.authz.dto.policy;

import org.datamate.authz.model.policy.enumtype.SubjectType;

/**
 * DTO representing a subject (Role or User) registered in the authorization store.
 *
 * @param subjectType The type of subject (ROLE or USER)
 * @param subjectId   The identifier of the subject (e.g. role name or user ID/email)
 */
public record SubjectDto(
        SubjectType subjectType,
        String subjectId
) {}
