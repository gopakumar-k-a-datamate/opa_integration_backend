package org.datamate.identity.application.port.out;

import org.datamate.identity.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserPersistencePort {
    void save(User user);
    Optional<User> findByEmail(String email);
    List<User> findAll();
}
