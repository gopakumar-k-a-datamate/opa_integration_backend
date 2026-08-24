package org.datamate.identity.role.application.port.out;

import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import org.datamate.identity.role.domain.model.Role;
import org.datamate.identity.role.application.query.RoleSearchCriteria;
import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface RolePersistencePort {
    Role save(Role role);
    Optional<Role> findById(UUID id);
    List<Role> findAll();
    List<Role> findAllByNameIn(Collection<String> names);
    Paged<Role> searchRoles(RoleSearchCriteria criteria, PageQuery pageQuery);
    List<Role> findActiveRoles(String search);
    void delete(UUID id);
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    List<String> findRoleNamesByUserId(UUID userId);
}


