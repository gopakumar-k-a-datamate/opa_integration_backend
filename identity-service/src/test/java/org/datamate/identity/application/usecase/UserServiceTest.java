package org.datamate.identity.application.usecase;

import org.datamate.identity.application.dto.user.UserResponseDto;
import org.datamate.identity.application.dto.user.UserSearchCriteria;
import org.datamate.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.application.usecase.user.UserService;
import org.datamate.identity.domain.model.User;
import org.datamate.identity.shared.model.UserStatus;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserPersistencePort userPort;
    private UserDtoMapper userDtoMapper;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userPort = mock(UserPersistencePort.class);
        userDtoMapper = mock(UserDtoMapper.class);
        userService = new UserService(userPort, userDtoMapper);
    }

    @Test
    void shouldSearchUsersAndReturnPagedResult() {
        UUID userId = UUID.randomUUID();
        User user = User.reconstitute(
                userId,
                "jane_doe",
                "jane@example.com",
                "+1987654321",
                "hashed_secret123",
                "Jane",
                "Doe",
                "ELLIDER",
                "EXT-12345",
                UserStatus.ACTIVE,
                List.of("USER"),
                0L,
                1L,
                "admin_user",
                LocalDateTime.now(),
                "admin_user",
                LocalDateTime.now()
        );

        Paged<User> pagedUser = new Paged<>(
                List.of(user),
                1,
                10,
                1L,
                1,
                false,
                false
        );

        UserSearchCriteria criteria = new UserSearchCriteria("jane", "USER", UserStatus.ACTIVE);
        PageQuery pageQuery = new PageQuery(1, 10);
        when(userPort.searchUsers(criteria, pageQuery)).thenReturn(pagedUser);

        Paged<UserResponseDto> result = userService.searchUsers(criteria, pageQuery);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("jane_doe", result.content().get(0).userName());
        assertEquals(UserStatus.ACTIVE, result.content().get(0).status());
        assertEquals(List.of("USER"), result.content().get(0).roles());
        assertEquals(1, result.pageNumber());
        assertEquals(10, result.pageSize());
        assertEquals(1L, result.totalElements());
    }
}
