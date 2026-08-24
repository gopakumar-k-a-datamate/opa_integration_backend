package org.datamate.identity.user.application.port.out;

import org.datamate.identity.user.domain.model.User;
import org.datamate.identity.user.application.query.UserSearchCriteria;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface UserPersistencePort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUserName(String userName);
    Optional<User> findByUserNameOrEmail(String userName, String email);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByUserNameAndIdNot(String userName, UUID id);
    List<User> findAll();
    Paged<User> searchUsers(UserSearchCriteria criteria, PageQuery pageQuery);
}


