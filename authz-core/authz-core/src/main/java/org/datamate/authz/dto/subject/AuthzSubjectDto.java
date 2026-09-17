package org.datamate.authz.dto.subject;

/**
 * Data Transfer Object for providing synced subjects to the Admin UI.
 *
 * <p>Used for display in policy assignment dropdowns and subject listings.
 * Fields not applicable to a subject type (e.g. {@code email} for a ROLE) will be {@code null}.</p>
 *
 * @param subjectId    IdP-issued UUID identifier
 * @param subjectName  Internal identifier (userName for users, role code for roles)
 * @param displayName  Human-readable label ("John Doe" for users, role name for roles)
 * @param email        Email address. {@code null} for ROLE subjects.
 * @param description  Role description. {@code null} for USER subjects.
 * @param status       Current status: {@code "ACTIVE"} or {@code "INACTIVE"}
 */
public record AuthzSubjectDto(
        String subjectId,
        String subjectName,
        String displayName,
        String email,
        String description,
        String status
) {}
