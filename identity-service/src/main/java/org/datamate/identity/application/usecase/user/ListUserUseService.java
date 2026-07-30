package org.datamate.identity.application.usecase.user;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.user.UserResponseDto;
import org.datamate.identity.application.dto.user.UserSearchCriteria;
import org.datamate.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.application.port.in.user.ListUserUseCase;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.model.User;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.PaginationHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListUserUseService implements ListUserUseCase {
    private final UserPersistencePort userPort;
    private final UserDtoMapper userDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public Paged<UserResponseDto> searchUsers(UserSearchCriteria criteria, PageQuery pageQuery) {
        int validatedPage = PaginationHelper.validatePageNumber(pageQuery.page());
        int validatedSize = PaginationHelper.validateLimit(pageQuery.size());
        PageQuery validatedPageQuery = new PageQuery(validatedPage, validatedSize);

        Paged<User> userPaged = userPort.searchUsers(criteria, validatedPageQuery);
        return userDtoMapper.toPaged(userPaged);
    }
}
