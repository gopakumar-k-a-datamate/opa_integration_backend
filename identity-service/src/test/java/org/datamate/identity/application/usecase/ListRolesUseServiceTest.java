package org.datamate.identity.application.usecase;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.query.role.RoleSearchCriteria;
import org.datamate.identity.application.mapper.role.RoleDtoMapper;
import org.datamate.identity.application.port.out.role.RolePersistencePort;
import org.datamate.identity.application.usecase.role.ListRolesUseService;
import org.datamate.identity.domain.model.Role;
import org.datamate.identity.shared.model.RoleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListRolesUseServiceTest {

    private RolePersistencePort rolePort;
    private RoleDtoMapper roleDtoMapper;
    private Logger log;
    private ListRolesUseService listRolesUseService;

    @BeforeEach
    void setUp() throws Exception {
        rolePort = mock(RolePersistencePort.class);
        roleDtoMapper = new RoleDtoMapper();
        log = mock(Logger.class);

        listRolesUseService = new ListRolesUseService(rolePort, roleDtoMapper);

        // Manually inject the logger mock because it's a non-constructor private field (@EnableLogger)
        Field logField = ListRolesUseService.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(listRolesUseService, log);
    }

    @Test
    void shouldListRolesSuccessfully() {
        // Arrange
        RoleSearchCriteria criteria = new RoleSearchCriteria("admin", RoleStatus.ACTIVE);
        Role sampleRole = Role.reconstitute(
                1L,
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

        when(rolePort.searchRoles(criteria)).thenReturn(List.of(sampleRole));

        // Act
        List<RoleDto> result = listRolesUseService.listRoles(criteria);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        RoleDto dto = result.get(0);
        assertEquals(1L, dto.id());
        assertEquals("ADMIN", dto.name());
        assertEquals("Administrator Role", dto.description());
        assertEquals(RoleStatus.ACTIVE, dto.status());

        verify(rolePort).searchRoles(criteria);
    }
}
