package org.datamate.authz.api.subject;

import org.datamate.authz.dto.subject.AuthzSubjectDto;
import org.datamate.authz.event.AuthzSubjectSyncEvent;
import org.datamate.authz.model.policy.enumtype.SubjectType;

import java.util.List;
import java.util.Optional;

/**
 * Core domain service contract for Subject Management.
 * 
 * <p>This interface provides unified capabilities for reading, validating, and updating
 * the local projection of subjects (users/roles) synced from the central Identity Service.</p>
 *
 * <p>Placed in the {@code api} package so it forms part of the public contract of
 * {@code authz-core}, adhering to the strict SPI and Unified Domain Services architecture.</p>
 */
public interface SubjectManagementService {

    /**
     * Idempotently applies an inbound sync event from the Identity Service to the local subject projection.
     *
     * @param event the inbound sync event
     */
    void apply(AuthzSubjectSyncEvent event);

    /**
     * Returns {@code true} if the given subject is known and currently active
     * (i.e. not soft-deleted) in the local projection. Used for policy validation.
     *
     * @param type      the subject type ({@code USER} or {@code ROLE})
     * @param subjectId the IdP-issued identifier
     * @return {@code true} when the subject exists and is active
     */
    boolean subjectExists(SubjectType type, String subjectId);

    /**
     * List all active (non-deleted) subjects of a given type.
     * 
     * @param type the subject type to filter by
     * @return list of active subjects
     */
    List<AuthzSubjectDto> listSubjects(SubjectType type);

    /**
     * Look up a single subject by its type and ID.
     * 
     * @param type the subject type
     * @param subjectId the subject ID
     * @return the subject DTO if found and active
     */
    Optional<AuthzSubjectDto> findSubject(SubjectType type, String subjectId);
}
