package org.datamate.identity.adapter.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.identity.application.command.CreateUserCommand;
import org.datamate.identity.application.dto.CreateUserRequest;
import org.datamate.identity.application.dto.UserDto;
import org.datamate.identity.application.port.in.UserManagementUseCase;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
                "securePass123"
        );

        UserDto responseDto = new UserDto(
                sampleId,
                "new_admin",
                "admin@example.com",
                "+1234567890",
                "Admin",
                "User",
                "SYSTEM_ADMIN",
                LocalDateTime.now()
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
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void shouldReturnBadRequestWhenMandatoryFieldsMissing() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "",
                "invalid-email",
                "+1234567890",
                "",
                "User",
                "123"
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
