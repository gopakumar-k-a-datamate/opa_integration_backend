package org.datamate.identity.application.port.out.role;

import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import org.datamate.identity.domain.model.Role;
import org.datamate.identity.application.query.role.RoleSearchCriteria;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface RolePersistencePort {
    Role save(Role role);
    Optional<Role> findById(UUID id);
    List<Role> findAll();
    Paged<Role> searchRoles(RoleSearchCriteria criteria, PageQuery pageQuery);
    List<Role> findActiveRoles(String search);
    void delete(UUID id);
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
    List<String> findRoleNamesByUserId(UUID userId);
}
