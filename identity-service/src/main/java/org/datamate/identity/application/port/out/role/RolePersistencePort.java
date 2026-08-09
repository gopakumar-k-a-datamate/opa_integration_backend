package org.datamate.identity.application.port.out.role;

import org.datamate.identity.domain.model.Role;
import org.datamate.identity.application.query.role.RoleSearchCriteria;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface RolePersistencePort {
    Role save(Role role);
    Optional<Role> findById(Long id);
    List<Role> findAll();
    List<Role> searchRoles(RoleSearchCriteria criteria);
    void delete(Long id);
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
    List<String> findRoleNamesByUserId(UUID userId);
}
