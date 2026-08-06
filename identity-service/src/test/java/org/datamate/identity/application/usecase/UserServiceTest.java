package org.datamate.identity.application.usecase;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.application.usecase.user.UserService;
import org.datamate.identity.domain.model.User;
import org.datamate.identity.shared.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserPersistencePort userPort;
    private UserDtoMapper userDtoMapper;
    private Logger log;
    private UserService userService;

    @BeforeEach
    void setUp() throws Exception {
        userPort = mock(UserPersistencePort.class);
        userDtoMapper = mock(UserDtoMapper.class);
        log = mock(Logger.class);

        userService = new UserService(userPort, userDtoMapper);

        // Manually inject the logger mock because it's a non-constructor private field (@EnableLogger)
        Field logField = UserService.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(userService, log);
    }

    @Test
    void shouldListUsersSuccessfully() {
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
                true,
                0L,
                1L,
                "admin_user",
                LocalDateTime.now(),
                "admin_user",
                LocalDateTime.now()
        );

        UserDto userDto = new UserDto(
                userId,
                "jane_doe",
                "jane@example.com",
                "+1987654321",
                "Jane",
                "Doe",
                "ELLIDER",
                "EXT-12345",
                "admin_user",
                LocalDateTime.now(),
                UserStatus.ACTIVE,
                List.of("USER"),
                true
        );

        when(userPort.findAll()).thenReturn(List.of(user));
        when(userDtoMapper.toDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.listUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("jane_doe", result.get(0).userName());
        assertTrue(result.get(0).passwordTemporary());
    }
}
