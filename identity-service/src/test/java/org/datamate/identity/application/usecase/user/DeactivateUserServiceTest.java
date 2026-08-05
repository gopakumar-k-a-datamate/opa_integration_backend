package org.datamate.identity.application.usecase.user;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.exception.user.UserNotFoundException;
import org.datamate.identity.domain.model.User;
import org.datamate.identity.shared.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeactivateUserServiceTest {

    private UserPersistencePort userPort;
    private Logger log;
    private DeactivateUserService deactivateUserService;

    @BeforeEach
    void setUp() throws Exception {
        userPort = mock(UserPersistencePort.class);
        log = mock(Logger.class);
        deactivateUserService = new DeactivateUserService(userPort);

        Field logField = DeactivateUserService.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(deactivateUserService, log);
    }

    @Test
    void shouldDeactivateUserSuccessfullyWhenUserExists() {
        UUID userId = UUID.randomUUID();
        User activeUser = User.reconstitute(
                userId, "active_user", "active@example.com", "+12345",
                "hash", "John", "Doe", "ELLIDER", "EXT-1",
                UserStatus.ACTIVE, new ArrayList<>(), false, 1L, 1L,
                "creator", LocalDateTime.now(), "creator", LocalDateTime.now()
        );

        when(userPort.findById(userId)).thenReturn(Optional.of(activeUser));

        deactivateUserService.deactivateUser(userId, "admin_user");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userPort).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser);
        assertEquals(UserStatus.INACTIVE, savedUser.getStatus());
        assertEquals("admin_user", savedUser.getLastModifiedBy());
        verify(log).info("Starting deactivation of user ID: {} by admin: {}", userId, "admin_user");
        verify(log).info("Successfully deactivated user ID: {} by admin: {}", userId, "admin_user");
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userPort.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> deactivateUserService.deactivateUser(userId, "admin_user"));

        verify(userPort, never()).save(any(User.class));
        verify(log).info("Starting deactivation of user ID: {} by admin: {}", userId, "admin_user");
        verify(log).error("User deactivation failed. User not found for ID: {}", userId);
    }
}
