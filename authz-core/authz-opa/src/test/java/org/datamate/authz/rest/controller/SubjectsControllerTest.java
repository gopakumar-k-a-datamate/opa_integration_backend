package org.datamate.authz.rest.controller;

import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.Paged;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.datamate.authz.api.subject.SubjectManagementService;
import org.datamate.authz.dto.subject.AuthzSubjectDto;
import org.datamate.authz.model.policy.enumtype.SubjectType;
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

class SubjectsControllerTest {

    private MockMvc mockMvc;
    private SubjectManagementService subjectService;
    private EndpointAuthorization authorization;

    @BeforeEach
    void setUp() {
        subjectService = mock(SubjectManagementService.class);
        authorization = mock(EndpointAuthorization.class);
        SubjectsController controller = new SubjectsController(subjectService, authorization);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getSubjects_paginated_returnsPagedObject() throws Exception {
        AuthzSubjectDto dto = new AuthzSubjectDto(
                "ACCOUNTANT", "Accountant", "Accountant Role", "acc@company.com", "Finance", "ACTIVE"
        );

        Paged<AuthzSubjectDto> paged = new Paged<>(
                List.of(dto), 1, 10, 1, 1, false, false
        );

        when(subjectService.getSubjects(eq(SubjectType.ROLE), eq("ACC"), any(PageQuery.class)))
                .thenReturn(paged);

        mockMvc.perform(get("/internal/authz/subjects")
                        .param("type", "ROLE")
                        .param("search", "ACC")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].subjectId").value("ACCOUNTANT"))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
