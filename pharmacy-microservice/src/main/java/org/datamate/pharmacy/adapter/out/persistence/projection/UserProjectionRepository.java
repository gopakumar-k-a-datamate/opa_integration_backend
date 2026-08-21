package org.datamate.pharmacy.adapter.out.persistence.projection;

import org.datamate.pharmacy.adapter.out.persistence.projection.entity.UserProjectionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProjectionRepository extends JpaRepository<UserProjectionJpaEntity, UUID> {
    Optional<UserProjectionJpaEntity> findByUsername(String username);
}
