package org.datamate.identity.adapter.in.rest.controller;

import com.datamate.bedrock.framework.common.auditing.annotation.AuditLog;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.user.CreateUserRequest;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.port.in.user.CreateUserUseCase;
import org.datamate.identity.application.dto.user.UserResponseDto;
import org.datamate.identity.application.dto.user.UserSearchCriteria;
import org.datamate.identity.application.port.in.user.ListUserUseCase;
import org.datamate.identity.application.port.in.user.GetUserUseCase;
import org.datamate.identity.shared.model.UserStatus;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    @EnableLogger
    private Logger log;
    private final ListUserUseCase listUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @AuditLog(action = "CREATE_USER", resource = "USER", description = "Create user account")
    @Operation(summary = "Create a new user", description = "Creates a new user account with the provided details such as username, email, password, and reference systems.")
    public UserDto createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Create user request received for '{}'", request.userName());
        return createUserUseCase.createUser(request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get user details", description = "Retrieve a user's detailed information by their unique ID.")
    public UserDto getUserById(@PathVariable UUID id) {
        log.info("Get user details request received for ID: {}", id);
        return getUserUseCase.getUserById(id);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "List users", description = "Search and filter user accounts using search query, role, and status, with support for pagination.")
    public Paged<UserResponseDto> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Search users request received [search: '{}', role: '{}', status: '{}', page: {}, size: {}]",
                search, role, status, page, size);
        UserSearchCriteria criteria = new UserSearchCriteria(search, role, status);
        PageQuery pageQuery = new PageQuery(page, size);
        Paged<UserResponseDto> result = listUserUseCase.searchUsers(criteria, pageQuery);
        log.info("Search users completed, returned {} of {} users", result.content().size(), result.totalElements());
        return result;
    }
}
