package org.datamate.authz.jpa.specification;

import org.datamate.authz.jpa.entity.AuthzSubjectJpaEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * Spring Data JPA Specifications for querying {@link AuthzSubjectJpaEntity}.
 */
public final class AuthzSubjectSpecification {

    private AuthzSubjectSpecification() {
        // Private constructor to prevent instantiation
    }

    public static Specification<AuthzSubjectJpaEntity> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<AuthzSubjectJpaEntity> hasSubjectType(String subjectType) {
        return (root, query, cb) -> {
            if (subjectType == null || subjectType.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("subjectType"), subjectType);
        };
    }

    public static Specification<AuthzSubjectJpaEntity> containsSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("subjectId")), pattern),
                    cb.like(cb.lower(root.get("displayName")), pattern),
                    cb.like(cb.lower(root.get("subjectName")), pattern)
            );
        };
    }
}
