package org.datamate.identity.adapter.out.persistence.user.specification;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.datamate.bedrock.framework.common.logging.util.LoggerManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.datamate.identity.adapter.out.persistence.entity.RoleJpaEntity;
import org.datamate.identity.adapter.out.persistence.user.entity.UserJpaEntity;
import org.datamate.identity.application.query.user.UserSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    private static Logger log;

    private static Logger logger() {
        if (log == null && LoggerManager.isInitialized()) {
            log = LoggerManager.getLogger(UserSpecification.class);
        }
        return log;
    }

    public static Specification<UserJpaEntity> filterUsers(UserSearchCriteria criteria) {
        Logger logger = logger();
        if (logger != null) {
            logger.debug("Building user search specification with criteria {}", criteria);
        }
        return (root, query, cb) -> {


            List<Predicate> predicates = new ArrayList<>();

            if (criteria.search() != null && !criteria.search().isBlank()) {
                String searchPattern = "%" + criteria.search().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("userName")), searchPattern),
                    cb.like(cb.lower(root.get("email")), searchPattern),
                    cb.like(cb.lower(root.get("firstName")), searchPattern),
                    cb.like(cb.lower(root.get("lastName")), searchPattern)
                ));
            }

            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }

            if (criteria.role() != null && !criteria.role().isBlank()) {
                Join<UserJpaEntity, RoleJpaEntity> rolesJoin = root.join("roles");
                predicates.add(cb.equal(cb.lower(rolesJoin.get("name")), criteria.role().toLowerCase()));
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
