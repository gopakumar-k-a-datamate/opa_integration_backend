package org.datamate.identity.adapter.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.identity.application.command.user.CreateUserCommand;
import org.datamate.identity.application.dto.user.CreateUserRequest;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.dto.user.UserResponseDto;
import org.datamate.identity.application.dto.user.UserSearchCriteria;
import org.datamate.identity.application.port.in.user.UserManagementUseCase;
import org.datamate.identity.shared.model.UserStatus;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserManagementUseCase userManagementUseCase;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .build();
    }

    @Test
    void shouldCreateUserSuccessfullyWhenValidRequestProvided() throws Exception {
        UUID sampleId = UUID.randomUUID();

        CreateUserRequest request = new CreateUserRequest(
                "new_admin",
                "admin@example.com",
                "+1234567890",
                "Admin",
                "User",
                "securePass123",
                "ELLIDER",
                "EXT-12345"
        );

        UserDto responseDto = new UserDto(
                sampleId,
                "new_admin",
                "admin@example.com",
                "+1234567890",
                "Admin",
                "User",
                "ELLIDER",
                "EXT-12345",
                "SYSTEM_ADMIN",
                LocalDateTime.now(),
                org.datamate.identity.shared.model.UserStatus.ACTIVE,
                java.util.List.of("ADMIN", "USER")
        );

        when(userManagementUseCase.createUser(any(CreateUserCommand.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sampleId.toString()))
                .andExpect(jsonPath("$.userName").value("new_admin"))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.referenceSystem").value("ELLIDER"))
                .andExpect(jsonPath("$.referenceValue").value("EXT-12345"));
    }

    @Test
    void shouldReturnBadRequestWhenMandatoryFieldsMissing() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "",
                "invalid-email",
                "+1234567890",
                "",
                "User",
                "123",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchUsersSuccessfully() throws Exception {
        UUID sampleId = UUID.randomUUID();
        UserResponseDto userResponseDto = new UserResponseDto(
                sampleId,
                "john_doe",
                "john@example.com",
                "John",
                "Doe",
                UserStatus.ACTIVE,
                List.of("USER")
        );

        Paged<UserResponseDto> pagedResult = new Paged<>(
                List.of(userResponseDto),
                1,
                10,
                1L,
                1,
                false,
                false
        );

        UserSearchCriteria criteria = new UserSearchCriteria("john", "USER", UserStatus.ACTIVE);
        when(userManagementUseCase.searchUsers(eq(criteria), any(PageQuery.class)))
                .thenReturn(pagedResult);

        mockMvc.perform(get("/api/v1/users/search")
                        .param("search", "john")
                        .param("role", "USER")
                        .param("status", "ACTIVE")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(sampleId.toString()))
                .andExpect(jsonPath("$.content[0].userName").value("john_doe"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].roles[0]").value("USER"))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
