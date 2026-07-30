package org.datamate.identity.adapter.in.rest.controller;

import com.datamate.bedrock.framework.common.auditing.annotation.AuditLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.user.CreateUserRequest;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.port.in.user.CreateUserUseCase;
import org.datamate.identity.application.port.in.user.UserManagementUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserManagementUseCase userManagementUseCase;
    private final CreateUserUseCase createUserUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    @AuditLog(action = "CREATE_USER", resource = "USER", resourceId = "#request.userName", description = "Create user account")
    public UserDto createUser(@Valid @RequestBody CreateUserRequest request) {
        return createUserUseCase.createUser(request);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(userManagementUseCase.listUsers());
    }
}
