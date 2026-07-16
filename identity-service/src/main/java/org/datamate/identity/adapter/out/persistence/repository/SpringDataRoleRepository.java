package org.datamate.identity.adapter.out.persistence.repository;

import org.datamate.identity.adapter.out.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRoleRepository extends JpaRepository<RoleJpaEntity, Long> {
    boolean existsByName(String name);
}
