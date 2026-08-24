package org.datamate.identity.user.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.user.application.dto.UserResponseDto;
import org.datamate.identity.user.application.query.UserSearchCriteria;
import org.datamate.identity.user.application.mapper.UserDtoMapper;
import org.datamate.identity.user.application.port.in.ListUserUseCase;
import org.datamate.identity.user.application.port.out.UserPersistencePort;
import org.datamate.identity.user.domain.model.User;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.PaginationHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListUserUseService implements ListUserUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPort;
    private final UserDtoMapper userDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public Paged<UserResponseDto> searchUsers(UserSearchCriteria criteria, PageQuery pageQuery) {
        log.info("Searching users with criteria {} and page query {}", criteria, pageQuery);
        int validatedPage = PaginationHelper.validatePageNumber(pageQuery.page());
        int validatedSize = PaginationHelper.validateLimit(pageQuery.size());
        PageQuery validatedPageQuery = new PageQuery(validatedPage, validatedSize);

        Paged<User> userPaged = userPort.searchUsers(criteria, validatedPageQuery);
        Paged<UserResponseDto> result = userDtoMapper.toPaged(userPaged);
        log.info("Search users completed, returned {} of {} users", result.content().size(), result.totalElements());
        return result;
    }
}


