package org.datamate.identity.application.port.out;

import org.datamate.identity.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserPersistencePort {
    User save(User user);
    Optional<User> findByUserName(String userName);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    List<User> findAll();
}
