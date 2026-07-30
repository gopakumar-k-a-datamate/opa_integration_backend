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
import org.datamate.identity.shared.model.UserStatus;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    @EnableLogger
    private Logger log;
    private final ListUserUseCase listUserUseCase;
    private final CreateUserUseCase createUserUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    @AuditLog(action = "CREATE_USER", resource = "USER", description = "Create user account")
    @Operation(summary = "Create a new user", description = "Creates a new user account with the provided details such as username, email, password, and reference systems.")
    public UserDto createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Create user request received for '{}'", request.userName());
        return createUserUseCase.createUser(request);
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
        UserSearchCriteria criteria = new UserSearchCriteria(search, role, status);
        PageQuery pageQuery = new PageQuery(page, size);
        return listUserUseCase.searchUsers(criteria, pageQuery);
    }
}
