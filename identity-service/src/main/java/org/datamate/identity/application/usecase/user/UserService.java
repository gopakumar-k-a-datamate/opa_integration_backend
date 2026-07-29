package org.datamate.identity.application.usecase.user;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.application.dto.user.UserResponseDto;
import org.datamate.identity.application.dto.user.UserSearchCriteria;
import org.datamate.identity.application.port.in.user.UserManagementUseCase;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.model.User;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.PaginationHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserManagementUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPort;
    private final UserDtoMapper userDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        List<UserDto> users = userPort.findAll().stream()
                .map(userDtoMapper::toDto)
                .toList();
        log.info("Listed {} users", users.size());
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public Paged<UserResponseDto> searchUsers(UserSearchCriteria criteria, PageQuery pageQuery) {
        int validatedPage = PaginationHelper.validatePageNumber(pageQuery.page());
        int validatedSize = PaginationHelper.validateLimit(pageQuery.size());
        PageQuery validatedPageQuery = new PageQuery(validatedPage, validatedSize);

        Paged<User> userPaged = userPort.searchUsers(criteria, validatedPageQuery);
        List<UserResponseDto> dtos = userPaged.content().stream()
                .map(this::mapToResponseDto)
                .toList();
        return new Paged<>(
                dtos,
                userPaged.pageNumber(),
                userPaged.pageSize(),
                userPaged.totalElements(),
                userPaged.totalPages(),
                userPaged.hasNext(),
                userPaged.hasPrevious()
        );
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFirstName(),
                user.getLastName(),
                user.getReferenceSystem(),
                user.getReferenceValue(),
                user.getCreatedBy(),
                user.getCreatedDate(),
                user.getStatus(),
                user.getRoles()
        );
    }

    private UserResponseDto mapToResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.getRoles()
        );
    }
}
