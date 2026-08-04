package org.datamate.authz.jpa.repository;

import org.datamate.authz.jpa.entity.PolicyBundleCacheJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataPolicyBundleCacheRepository
        extends JpaRepository<PolicyBundleCacheJpaEntity, Long> {

    /** Returns the single bundle row for a given namespace. */
    Optional<PolicyBundleCacheJpaEntity> findByNamespace(String namespace);
}

