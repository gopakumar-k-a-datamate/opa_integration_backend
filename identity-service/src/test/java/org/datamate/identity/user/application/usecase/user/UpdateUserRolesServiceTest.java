package org.datamate.identity.user.application.usecase.user;

import org.datamate.identity.user.application.dto.user.UpdateUserRolesRequest;
import org.datamate.identity.user.application.dto.user.UserDto;
import org.datamate.identity.user.application.mapper.user.UserDtoMapper;
import org.datamate.identity.role.application.port.out.role.RolePersistencePort;
import org.datamate.identity.user.application.port.out.user.UserPersistencePort;
import org.datamate.identity.role.domain.exception.role.RoleNotFoundException;
import org.datamate.identity.user.domain.exception.user.InvalidRoleAssignmentException;
import org.datamate.identity.user.domain.exception.user.UserNotFoundException;
import org.datamate.identity.role.domain.model.role.entity.Role;
import org.datamate.identity.user.domain.model.user.entity.User;
import org.datamate.identity.shared.model.RoleStatus;
import org.datamate.identity.shared.model.UserStatus;
import org.datamate.identity.user.domain.event.user.UserRolesUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import com.datamate.bedrock.framework.common.ddd.datatype.EntityReference;
import com.datamate.bedrock.framework.common.ddd.datatype.ResourceIdentifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateUserRolesServiceTest {

    private UserPersistencePort userPort;
    private RolePersistencePort rolePort;
    private UserDtoMapper userDtoMapper;
    private ApplicationEventPublisher eventPublisher;
    private UpdateUserRolesService service;
    private final EntityReference<UUID> auditRef = new EntityReference<>(null, new ResourceIdentifier("system", "creator"));

    @BeforeEach
    void setUp() {
        userPort = mock(UserPersistencePort.class);
        rolePort = mock(RolePersistencePort.class);
        userDtoMapper = new UserDtoMapper();
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new UpdateUserRolesService(userPort, rolePort, userDtoMapper, eventPublisher);
    }

    @Test
    void shouldUpdateUserRolesSuccessfully() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.reconstitute(
                userId, "test_user", "test@example.com", "+12345",
                "hash", "John", "Doe", "ELLIDER", "EXT-1",
                UserStatus.ACTIVE, new ArrayList<>(), false, 1L, 1L,
                "creator", LocalDateTime.now(), "creator", LocalDateTime.now()
        );

        Role adminRole = Role.reconstitute(
                UUID.randomUUID(), "ADMIN", "Administrator Role", RoleStatus.ACTIVE,
                null, null, 1L, 1L, auditRef, LocalDateTime.now(), auditRef, LocalDateTime.now()
        );

        when(userPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(rolePort.findAll()).thenReturn(List.of(adminRole));
        when(userPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRolesRequest request = new UpdateUserRolesRequest(List.of("ADMIN"));
        UserDto result = service.updateUserRoles(userId, request, "admin_user");

        assertNotNull(result);
        assertEquals(List.of("ADMIN"), result.roles());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userPort).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(List.of("ADMIN"), savedUser.getRoles());

        verify(eventPublisher).publishEvent(any(UserRolesUpdatedEvent.class));
    }

    @Test
    void shouldRemoveRolesSuccessfully() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.reconstitute(
                userId, "test_user", "test@example.com", "+12345",
                "hash", "John", "Doe", "ELLIDER", "EXT-1",
                UserStatus.ACTIVE, List.of("ADMIN"), false, 1L, 1L,
                "creator", LocalDateTime.now(), "creator", LocalDateTime.now()
        );

        when(userPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(rolePort.findAll()).thenReturn(new ArrayList<>());
        when(userPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRolesRequest request = new UpdateUserRolesRequest(new ArrayList<>());
        UserDto result = service.updateUserRoles(userId, request, "admin_user");

        assertNotNull(result);
        assertTrue(result.roles().isEmpty());

        verify(eventPublisher).publishEvent(any(UserRolesUpdatedEvent.class));
    }

    @Test
    void shouldPreventDuplicatesInRoleAssignment() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.reconstitute(
                userId, "test_user", "test@example.com", "+12345",
                "hash", "John", "Doe", "ELLIDER", "EXT-1",
                UserStatus.ACTIVE, new ArrayList<>(), false, 1L, 1L,
                "creator", LocalDateTime.now(), "creator", LocalDateTime.now()
        );

        Role adminRole = Role.reconstitute(
                UUID.randomUUID(), "ADMIN", "Administrator Role", RoleStatus.ACTIVE,
                null, null, 1L, 1L, auditRef, LocalDateTime.now(), auditRef, LocalDateTime.now()
        );

        when(userPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(rolePort.findAll()).thenReturn(List.of(adminRole));
        when(userPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRolesRequest request = new UpdateUserRolesRequest(List.of("ADMIN", "ADMIN"));
        UserDto result = service.updateUserRoles(userId, request, "admin_user");

        assertNotNull(result);
        assertEquals(List.of("ADMIN"), result.roles());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userPort.findById(userId)).thenReturn(Optional.empty());

        UpdateUserRolesRequest request = new UpdateUserRolesRequest(List.of("ADMIN"));
        assertThrows(UserNotFoundException.class, () -> service.updateUserRoles(userId, request, "admin_user"));
    }

    @Test
    void shouldThrowRoleNotFoundExceptionWhenRoleDoesNotExist() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.reconstitute(
                userId, "test_user", "test@example.com", "+12345",
                "hash", "John", "Doe", "ELLIDER", "EXT-1",
                UserStatus.ACTIVE, new ArrayList<>(), false, 1L, 1L,
                "creator", LocalDateTime.now(), "creator", LocalDateTime.now()
        );

        when(userPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(rolePort.findAll()).thenReturn(new ArrayList<>());

        UpdateUserRolesRequest request = new UpdateUserRolesRequest(List.of("NON_EXISTENT"));
        assertThrows(RoleNotFoundException.class, () -> service.updateUserRoles(userId, request, "admin_user"));
    }

    @Test
    void shouldThrowInvalidRoleAssignmentExceptionWhenRoleIsInactive() {
        UUID userId = UUID.randomUUID();
        User existingUser = User.reconstitute(
                userId, "test_user", "test@example.com", "+12345",
                "hash", "John", "Doe", "ELLIDER", "EXT-1",
                UserStatus.ACTIVE, new ArrayList<>(), false, 1L, 1L,
                "creator", LocalDateTime.now(), "creator", LocalDateTime.now()
        );

        Role inactiveRole = Role.reconstitute(
                UUID.randomUUID(), "INACTIVE_ROLE", "Inactive Role", RoleStatus.INACTIVE,
                null, null, 1L, 1L, auditRef, LocalDateTime.now(), auditRef, LocalDateTime.now()
        );

        when(userPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(rolePort.findAll()).thenReturn(List.of(inactiveRole));

        UpdateUserRolesRequest request = new UpdateUserRolesRequest(List.of("INACTIVE_ROLE"));
        assertThrows(InvalidRoleAssignmentException.class, () -> service.updateUserRoles(userId, request, "admin_user"));
    }
}
