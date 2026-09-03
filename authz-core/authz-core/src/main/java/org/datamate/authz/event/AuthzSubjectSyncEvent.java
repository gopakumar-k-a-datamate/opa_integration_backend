package org.datamate.authz.event;

/**
 * Framework-internal event representing a subject lifecycle change synced from the Identity Service.
 *
 * <h2>Role</h2>
 * <p>This record is the {@code authz-core} framework's internal model for subject sync events.
 * It is <strong>not</strong> a shared cross-service messaging contract.
 * Consumer services receive the raw JSON from RabbitMQ into their own local DTO
 * (e.g., {@code SubjectSyncMessage}) and map it to this record before calling
 * {@link org.datamate.authz.api.subject.SubjectManagementService#apply(AuthzSubjectSyncEvent)}.</p>
 *
 * <h2>Unified Subject Model</h2>
 * <p>Both USER and ROLE subjects are stored uniformly. Fields not applicable to a subject type
 * (e.g. {@code email} for a ROLE) must be passed as {@code null}.</p>
 *
 * <h2>Wire Contract</h2>
 * <p>The JSON format published on the {@code auth.subject.sync} RabbitMQ exchange is defined
 * in {@code docs/contracts/subject-sync-wire-contract.md}. Consumer services must align their
 * local DTO field names with that document — not with this class.</p>
 *
 * @param subjectType   {@code "USER"} or {@code "ROLE"} (open-ended for future types)
 * @param subjectId     IdP-issued UUID identifier
 * @param subjectName   Internal identifier: {@code userName} for users, role code/name for roles
 * @param displayName   Human-readable name: "John Doe" for users, role name for roles
 * @param email         User email address. {@code null} for ROLE subjects.
 * @param description   Role description. {@code null} for USER subjects.
 * @param status        Identity Service status string: {@code "ACTIVE"} or {@code "INACTIVE"}
 * @param version       Monotonically increasing domain version; guards out-of-order delivery
 * @param deleted       {@code true} = subject was deactivated (soft-delete the local copy)
 */
public record AuthzSubjectSyncEvent(
        String subjectType,
        String subjectId,
        String subjectName,
        String displayName,
        String email,
        String description,
        String status,
        long version,
        boolean deleted
) {}
