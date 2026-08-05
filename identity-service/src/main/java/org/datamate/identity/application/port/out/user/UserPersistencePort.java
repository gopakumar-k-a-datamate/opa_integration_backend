package org.datamate.identity.application.port.out.user;

import org.datamate.identity.domain.model.User;
import org.datamate.identity.application.query.user.UserSearchCriteria;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface UserPersistencePort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUserName(String userName);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    List<User> findAll();
    Paged<User> searchUsers(UserSearchCriteria criteria, PageQuery pageQuery);
}
