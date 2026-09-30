package org.datamate.authz.jpa.specification;

import org.datamate.authz.jpa.entity.PolicyJpaEntity;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Spring Data JPA Specifications for querying {@link PolicyJpaEntity}.
 */
public final class PolicySpecification {

    private PolicySpecification() {
        // Private constructor to prevent instantiation
    }

    public static Specification<PolicyJpaEntity> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<PolicyJpaEntity> hasSubjectType(SubjectType subjectType) {
        return (root, query, cb) -> {
            if (subjectType == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("subjectType"), subjectType);
        };
    }

    public static Specification<PolicyJpaEntity> containsSubjectSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("subjectId")), pattern);
        };
    }
}
