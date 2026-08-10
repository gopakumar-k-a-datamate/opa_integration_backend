package org.datamate.identity.application.usecase;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.mapper.role.RoleDtoMapper;
import org.datamate.identity.application.port.out.role.RolePersistencePort;
import org.datamate.identity.application.port.out.SecurityContextPort;
import org.datamate.identity.application.usecase.role.RoleManagementService;
import org.datamate.identity.domain.model.Role;
import org.datamate.identity.shared.model.RoleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleManagementServiceTest {

    private RolePersistencePort rolePort;
    private SecurityContextPort securityContextPort;
    private RoleDtoMapper roleDtoMapper;
    private Logger log;
    private RoleManagementService roleManagementService;

    @BeforeEach
    void setUp() throws Exception {
        rolePort = mock(RolePersistencePort.class);
        securityContextPort = mock(SecurityContextPort.class);
        roleDtoMapper = new RoleDtoMapper();
        log = mock(Logger.class);

        roleManagementService = new RoleManagementService(rolePort, securityContextPort, roleDtoMapper);

        // Manually inject the logger mock because it's a non-constructor private field (@EnableLogger)
        Field logField = RoleManagementService.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(roleManagementService, log);
    }

    @Test
    void shouldGetRoleSuccessfully() {
        UUID roleId = UUID.randomUUID();
        Role sampleRole = Role.reconstitute(
                roleId,
                "ADMIN",
                "Administrator Role",
                RoleStatus.ACTIVE,
                null,
                null,
                1L,
                1L,
                "system",
                LocalDateTime.now(),
                "system",
                LocalDateTime.now()
        );

        when(rolePort.findById(roleId)).thenReturn(Optional.of(sampleRole));

        RoleDto result = roleManagementService.getRole(roleId);

        assertNotNull(result);
        assertEquals(roleId, result.id());
        assertEquals("ADMIN", result.name());
        verify(rolePort).findById(roleId);
    }

    @Test
    void shouldDeleteRoleSuccessfully() {
        UUID roleId = UUID.randomUUID();

        roleManagementService.deleteRole(roleId);

        verify(rolePort).delete(roleId);
    }
}
