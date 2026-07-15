package org.datamate.identity.application.port.out;

import org.datamate.identity.domain.model.Role;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface RolePersistencePort {
    Role save(Role role);
    Optional<Role> findById(UUID id);
    List<Role> findAll();
    void delete(UUID id);
    boolean existsByName(String name);
}
