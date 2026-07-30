package org.datamate.identity.application.usecase;

import org.datamate.identity.application.command.user.CreateUserCommand;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.application.port.out.PasswordEncoderPort;
import org.datamate.identity.application.port.out.SecurityContextPort;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.application.usecase.user.CreateUserService;
import org.datamate.identity.shared.event.user.UserCreatedEvent;
import org.datamate.identity.domain.exception.user.UserAlreadyExistsException;
import org.datamate.identity.domain.model.User;
import org.datamate.identity.shared.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateUserServiceTest {

    private UserPersistencePort userPort;
    private PasswordEncoderPort passwordEncoderPort;
    private SecurityContextPort securityContextPort;
    private ApplicationEventPublisher eventPublisher;
    private UserDtoMapper userDtoMapper;
    private CreateUserService createUserService;

    @BeforeEach
    void setUp() {
        userPort = mock(UserPersistencePort.class);
        passwordEncoderPort = mock(PasswordEncoderPort.class);
        securityContextPort = mock(SecurityContextPort.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        userDtoMapper = new UserDtoMapper();

        createUserService = new CreateUserService(userPort, passwordEncoderPort, securityContextPort, eventPublisher, userDtoMapper);
    }

    @Test
    void shouldCreateUserSuccessfullyWhenValidCommandProvided() {
        CreateUserCommand command = new CreateUserCommand(
                "jane_doe",
                "jane@example.com",
                "+1987654321",
                "Jane",
                "Doe",
                "secret123",
                "ELLIDER",
                "EXT-12345"
        );

        UUID userId = UUID.randomUUID();

        when(userPort.existsByUserName("jane_doe")).thenReturn(false);
        when(userPort.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoderPort.encode("secret123")).thenReturn("hashed_secret123");
        when(securityContextPort.getCurrentUsername()).thenReturn("admin_user");

        User savedUserMock = User.reconstitute(
                userId,
                "jane_doe",
                "jane@example.com",
                "+1987654321",
                "hashed_secret123",
                "Jane",
                "Doe",
                "ELLIDER",
                "EXT-12345",
                org.datamate.identity.shared.model.UserStatus.ACTIVE,
                java.util.Collections.emptyList(),
                0L,
                1L,
                "admin_user",
                LocalDateTime.now(),
                "admin_user",
                LocalDateTime.now()
        );

        when(userPort.save(any(User.class))).thenReturn(savedUserMock);

        UserDto result = createUserService.createUser(command);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("jane_doe", result.userName());
        assertEquals("jane@example.com", result.email());
        assertEquals("+1987654321", result.phoneNumber());
        assertEquals("Jane", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("ELLIDER", result.referenceSystem());
        assertEquals("EXT-12345", result.referenceValue());
        assertEquals("admin_user", result.createdBy());

        verify(userPort).save(any(User.class));
        verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
    }

    @Test
    void shouldThrowUserAlreadyExistsExceptionWhenUserNameExists() {
        CreateUserCommand command = new CreateUserCommand(
                "jane_doe",
                "jane@example.com",
                "+1987654321",
                "Jane",
                "Doe",
                "secret123",
                null,
                null
        );

        when(userPort.existsByUserName("jane_doe")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> createUserService.createUser(command)
        );

        assertEquals("user.alreadyExists", exception.getErrorCode());
        verify(userPort, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowUserAlreadyExistsExceptionWhenEmailExists() {
        CreateUserCommand command = new CreateUserCommand(
                "jane_doe",
                "jane@example.com",
                "+1987654321",
                "Jane",
                "Doe",
                "secret123",
                null,
                null
        );

        when(userPort.existsByUserName("jane_doe")).thenReturn(false);
        when(userPort.existsByEmail("jane@example.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> createUserService.createUser(command)
        );

        assertEquals("user.alreadyExists", exception.getErrorCode());
        verify(userPort, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any());
    }
}
