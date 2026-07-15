package org.datamate.identity.adapter.out.persistence.repository;

import org.datamate.identity.adapter.out.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataRoleRepository extends JpaRepository<RoleJpaEntity, UUID> {
    boolean existsByName(String name);
}
