package org.datamate.identity.adapter.in.rest.controller;

import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.dto.role.RoleRequest;
import org.datamate.identity.application.port.in.role.CreateRoleUseCase;
import org.datamate.identity.application.port.in.role.ListRolesUseCase;
import org.datamate.identity.application.port.in.role.RoleManagementUseCase;
import org.datamate.identity.application.query.role.RoleSearchCriteria;
import org.datamate.identity.shared.model.RoleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RoleManagementUseCase roleManagementUseCase;

    @Mock
    private CreateRoleUseCase createRoleUseCase;

    @Mock
    private ListRolesUseCase listRolesUseCase;

    @Mock
    private Logger log;

    @InjectMocks
    private RoleController roleController;

    @BeforeEach
    void setUp() throws Exception {
        // Manually inject the logger mock because it's a non-constructor private field (@EnableLogger)
        Field logField = RoleController.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(roleController, log);

        mockMvc = MockMvcBuilders.standaloneSetup(roleController)
                .build();
    }

    @Test
    void shouldCreateRoleSuccessfullyWhenValidRequestProvided() throws Exception {
        RoleRequest request = new RoleRequest("DENTIST", "Clinical Dentist Role");
        RoleDto responseDto = new RoleDto(1L, "DENTIST", "Clinical Dentist Role", RoleStatus.INACTIVE);

        when(createRoleUseCase.createRole(any(RoleRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("DENTIST"))
                .andExpect(jsonPath("$.description").value("Clinical Dentist Role"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldReturnBadRequestWhenMandatoryFieldsMissing() throws Exception {
        RoleRequest request = new RoleRequest("", "No Name Role");

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetRoleSuccessfully() throws Exception {
        RoleDto responseDto = new RoleDto(1L, "DENTIST", "Clinical Dentist Role", RoleStatus.ACTIVE);
        when(roleManagementUseCase.getRole(eq(1L))).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/roles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("DENTIST"));
    }

    @Test
    void shouldListRolesSuccessfully() throws Exception {
        RoleDto responseDto = new RoleDto(1L, "DENTIST", "Clinical Dentist Role", RoleStatus.ACTIVE);
        when(listRolesUseCase.listRoles(any(RoleSearchCriteria.class))).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("DENTIST"));
    }

    @Test
    void shouldDeleteRoleSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(roleManagementUseCase).deleteRole(eq(1L));
    }
}
