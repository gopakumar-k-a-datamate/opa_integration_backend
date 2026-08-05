package org.datamate.identity.adapter.out.persistence.repository;

import org.datamate.identity.adapter.out.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SpringDataRoleRepository extends JpaRepository<RoleJpaEntity, Long> {
    boolean existsByName(String name);

    @Query(value = "SELECT r.name FROM role r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = :userId", nativeQuery = true)
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);
}
