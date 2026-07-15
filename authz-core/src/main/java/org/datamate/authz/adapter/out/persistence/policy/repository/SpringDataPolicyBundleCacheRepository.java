package org.datamate.authz.adapter.out.persistence.policy.repository;

import org.datamate.authz.adapter.out.persistence.policy.entity.PolicyBundleCacheJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPolicyBundleCacheRepository
        extends JpaRepository<PolicyBundleCacheJpaEntity, UUID> {

    /** Returns the single bundle row. There is at most one row per service database. */
    Optional<PolicyBundleCacheJpaEntity> findFirstByOrderByCreatedAtDesc();
}

