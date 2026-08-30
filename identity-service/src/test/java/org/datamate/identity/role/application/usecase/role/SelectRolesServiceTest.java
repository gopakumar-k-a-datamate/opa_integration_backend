package org.datamate.identity.role.application.usecase.role;

import com.datamate.bedrock.framework.common.ddd.datatype.EntityReference;
import com.datamate.bedrock.framework.common.ddd.datatype.ResourceIdentifier;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.role.application.dto.role.RoleSelectDto;
import org.datamate.identity.role.application.mapper.role.RoleDtoMapper;
import org.datamate.identity.role.application.port.out.role.RolePersistencePort;
import org.datamate.identity.role.domain.model.role.entity.Role;
import org.datamate.identity.role.domain.model.role.enums.RoleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SelectRolesServiceTest {

    private RolePersistencePort rolePort;
    private RoleDtoMapper roleDtoMapper;
    private Logger log;
    private SelectRolesService selectRolesService;

    @BeforeEach
    void setUp() throws Exception {
        rolePort = mock(RolePersistencePort.class);
        roleDtoMapper = new RoleDtoMapper();
        log = mock(Logger.class);

        selectRolesService = new SelectRolesService(rolePort, roleDtoMapper);

        // Manually inject the logger mock because it's a non-constructor private field (@EnableLogger)
        Field logField = SelectRolesService.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(selectRolesService, log);
    }

    @Test
    void shouldSelectRolesSuccessfully() {
        // Arrange
        UUID roleId = UUID.randomUUID();
        EntityReference<UUID> auditRef = new EntityReference<>(
                null,
                new ResourceIdentifier("system", "system")
        );
        Role sampleRole = Role.reconstitute(
                roleId,
                "ADMIN",
                "Administrator Role",
                RoleStatus.ACTIVE,
                null,
                null,
                1L,
                1L,
                auditRef,
                LocalDateTime.now(),
                auditRef,
                LocalDateTime.now()
        );

        when(rolePort.findActiveRoles("ADM")).thenReturn(List.of(sampleRole));

        // Act
        List<RoleSelectDto> result = selectRolesService.selectRoles("ADM");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        RoleSelectDto dto = result.get(0);
        assertEquals(roleId, dto.id());
        assertEquals("ADMIN", dto.name());

        verify(rolePort).findActiveRoles("ADM");
    }
}
