package org.datamate.identity.role.application.usecase;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.role.application.port.out.RolePersistencePort;
import org.datamate.identity.role.application.usecase.RoleManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.mockito.Mockito.*;

class RoleManagementServiceTest {

    private RolePersistencePort rolePort;
    private Logger log;
    private RoleManagementService roleManagementService;

    @BeforeEach
    void setUp() throws Exception {
        rolePort = mock(RolePersistencePort.class);
        log = mock(Logger.class);

        roleManagementService = new RoleManagementService(rolePort);

        // Manually inject the logger mock because it's a non-constructor private field (@EnableLogger)
        Field logField = RoleManagementService.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(roleManagementService, log);
    }

    @Test
    void shouldDeleteRoleSuccessfully() {
        UUID roleId = UUID.randomUUID();

        roleManagementService.deleteRole(roleId);

        verify(rolePort).delete(roleId);
    }
}


