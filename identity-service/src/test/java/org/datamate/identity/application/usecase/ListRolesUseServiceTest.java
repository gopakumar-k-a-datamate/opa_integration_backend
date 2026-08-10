package org.datamate.identity.application.usecase;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.query.role.RoleSearchCriteria;
import org.datamate.identity.application.mapper.role.RoleDtoMapper;
import org.datamate.identity.application.port.out.role.RolePersistencePort;
import org.datamate.identity.application.usecase.role.ListRolesUseService;
import org.datamate.identity.domain.model.Role;
import org.datamate.identity.shared.model.RoleStatus;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
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
        PageQuery pageQuery = new PageQuery(1, 10);
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

        Paged<Role> pagedRole = new Paged<>(List.of(sampleRole), 1, 10, 1L, 1, false, false);
        when(rolePort.searchRoles(eq(criteria), any(PageQuery.class))).thenReturn(pagedRole);

        // Act
        Paged<RoleDto> result = listRolesUseService.listRoles(criteria, pageQuery);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
        RoleDto dto = result.content().get(0);
        assertEquals(1L, dto.id());
        assertEquals("ADMIN", dto.name());
        assertEquals("Administrator Role", dto.description());
        assertEquals(RoleStatus.ACTIVE, dto.status());

        verify(rolePort).searchRoles(eq(criteria), any(PageQuery.class));
    }
}
