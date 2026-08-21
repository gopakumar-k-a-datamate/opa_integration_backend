package org.datamate.pharmacy.adapter.out.persistence.projection;

import org.datamate.pharmacy.adapter.out.persistence.projection.entity.RoleProjectionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleProjectionRepository extends JpaRepository<RoleProjectionJpaEntity, UUID> {
    Optional<RoleProjectionJpaEntity> findByName(String name);
}
