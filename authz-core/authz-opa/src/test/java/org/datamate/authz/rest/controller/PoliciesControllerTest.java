package org.datamate.authz.rest.controller;

import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.Paged;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.dto.policy.PolicySearchQuery;
import org.datamate.authz.service.policy.PolicyManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PoliciesControllerTest {

    private MockMvc mockMvc;
    private PolicyManagementService policyService;
    private EndpointAuthorization authorization;

    @BeforeEach
    void setUp() {
        policyService = mock(PolicyManagementService.class);
        authorization = mock(EndpointAuthorization.class);
        PoliciesController controller = new PoliciesController(policyService, authorization);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getPolicies_unpaginated_returnsList() throws Exception {
        PolicyGridItemDto item = new PolicyGridItemDto(
                "pharmacy:prescription:create", "create", "pharmacy", "prescription",
                1L, null, null, true, null, null, false, false, null
        );

        when(policyService.getPolicies(any(PolicySearchQuery.class)))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/internal/authz/policies")
                        .param("subjectType", "ROLE")
                        .param("subjectId", "ACCOUNTANT")
                        .param("namespace", "pharmacy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].permissionCode").value("pharmacy:prescription:create"));
    }

    @Test
    void getPolicies_paginated_returnsPagedObject() throws Exception {
        PolicyGridItemDto item = new PolicyGridItemDto(
                "pharmacy:prescription:create", "create", "pharmacy", "prescription",
                1L, null, null, true, null, null, false, false, null
        );

        Paged<PolicyGridItemDto> paged = new Paged<>(
                List.of(item), 1, 5, 1, 1, false, false
        );

        when(policyService.getPolicies(any(PolicySearchQuery.class), any(PageQuery.class)))
                .thenReturn(paged);

        mockMvc.perform(get("/internal/authz/policies")
                        .param("subjectType", "ROLE")
                        .param("subjectId", "ACCOUNTANT")
                        .param("namespace", "pharmacy")
                        .param("search", "prescription")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].permissionCode").value("pharmacy:prescription:create"))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
