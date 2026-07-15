package org.datamate.identity.application.port.out;

import org.datamate.identity.domain.model.Role;
import java.util.Optional;
import java.util.List;

public interface RolePersistencePort {
    Role save(Role role);
    Optional<Role> findById(Long id);
    List<Role> findAll();
    void delete(Long id);
    boolean existsByName(String name);
}
