package org.datamate.identity.adapter.out.persistence.adapter.user;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.adapter.out.persistence.entity.user.UserJpaEntity;
import org.datamate.identity.adapter.out.persistence.mapper.user.UserPersistenceMapper;
import org.datamate.identity.adapter.out.persistence.repository.user.SpringDataUserRepository;
import org.datamate.identity.adapter.out.persistence.specification.user.UserSpecification;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.application.dto.user.UserSearchCriteria;
import org.datamate.identity.domain.model.User;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.datamate.identity.shared.pagination.PaginationHelperMethods.toPageable;
import static org.datamate.identity.shared.pagination.PaginationHelperMethods.toPaged;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    @EnableLogger
    private Logger log;

    private final SpringDataUserRepository repository;
    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.mapToJpaEntity(user);
        UserJpaEntity savedEntity = repository.save(entity);
        log.debug("User '{}' persisted with id {}", savedEntity.getUserName(), savedEntity.getId());
        return mapper.mapToDomain(savedEntity);
    }

    @Override
    public Optional<User> findByUserName(String userName) {
        return repository.findByUserName(userName).map(mapper::mapToDomain);
    }

    @Override
    public boolean existsByUserName(String userName) {
        return repository.existsByUserName(userName);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(mapper::mapToDomain).toList();
    }

    @Override
    public Paged<User> searchUsers(UserSearchCriteria criteria, PageQuery pageQuery) {
        Pageable pageable = toPageable(pageQuery, Sort.by(Sort.Direction.DESC, "id"));
        Page<UserJpaEntity> entityPage = repository.findAll(
                UserSpecification.filterUsers(criteria),
                pageable
        );
        return toPaged(entityPage.map(mapper::mapToDomain));
    }
}
