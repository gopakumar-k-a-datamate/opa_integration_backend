package org.datamate.identity.role.application.usecase;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.role.application.port.out.RolePersistencePort;
import org.datamate.identity.role.application.usecase.ActivateRoleService;
import org.datamate.identity.role.domain.exception.RoleNotFoundException;
import org.datamate.identity.role.domain.exception.InvalidRoleDataException;
import org.datamate.identity.role.domain.model.Role;
import org.datamate.identity.role.shared.model.RoleStatus;
import org.datamate.identity.role.shared.event.role.RoleActivatedEvent;
import com.datamate.bedrock.framework.common.ddd.datatype.EntityReference;
import com.datamate.bedrock.framework.common.ddd.datatype.ResourceIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class ActivateRoleServiceTest {

    private RolePersistencePort rolePort;
    private ApplicationEventPublisher eventPublisher;
    private Logger log;
    private ActivateRoleService activateRoleService;

    private final UUID roleId = UUID.randomUUID();
    private final EntityReference<UUID> adminUserRef = new EntityReference<>(
            UUID.randomUUID(),
            new ResourceIdentifier("identity-service", "admin")
    );

    @BeforeEach
    void setUp() throws Exception {
        rolePort = mock(RolePersistencePort.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        log = mock(Logger.class);

        activateRoleService = new ActivateRoleService(rolePort, eventPublisher);

        Field logField = ActivateRoleService.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(activateRoleService, log);
    }

    @Test
    void shouldActivateRoleSuccessfullyWhenRoleExistsAndIsInactive() {
        // Arrange
        Role sampleRole = Role.reconstitute(
                roleId,
                "TEST_ROLE",
                "Test Description",
                RoleStatus.INACTIVE,
                null,
                null,
                1L,
                1L,
                adminUserRef,
                LocalDateTime.now(),
                adminUserRef,
                LocalDateTime.now()
        );

        when(rolePort.findById(roleId)).thenReturn(Optional.of(sampleRole));
        when(rolePort.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        activateRoleService.activateRole(roleId, adminUserRef);

        // Assert
        verify(rolePort).findById(roleId);
        verify(rolePort).save(argThat(role -> role.getStatus() == RoleStatus.ACTIVE));
        verify(eventPublisher).publishEvent(any(RoleActivatedEvent.class));
    }

    @Test
    void shouldThrowInvalidRoleDataExceptionWhenRoleIsAlreadyActive() {
        // Arrange
        Role sampleRole = Role.reconstitute(
                roleId,
                "TEST_ROLE",
                "Test Description",
                RoleStatus.ACTIVE,
                null,
                null,
                1L,
                1L,
                adminUserRef,
                LocalDateTime.now(),
                adminUserRef,
                LocalDateTime.now()
        );

        when(rolePort.findById(roleId)).thenReturn(Optional.of(sampleRole));

        // Act & Assert
        assertThrows(InvalidRoleDataException.class, () -> activateRoleService.activateRole(roleId, adminUserRef));
        verify(rolePort).findById(roleId);
        verify(rolePort, never()).save(any(Role.class));
        verify(eventPublisher, never()).publishEvent(any(RoleActivatedEvent.class));
    }

    @Test
    void shouldThrowRoleNotFoundExceptionWhenRoleDoesNotExist() {
        // Arrange
        when(rolePort.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RoleNotFoundException.class, () -> activateRoleService.activateRole(roleId, adminUserRef));
        verify(rolePort).findById(roleId);
        verify(rolePort, never()).save(any(Role.class));
    }
}


