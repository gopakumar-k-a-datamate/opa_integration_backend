package org.datamate.identity.adapter.in.rest.controller;

import com.datamate.bedrock.framework.common.auditing.annotation.AuditLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.command.CreateUserCommand;
import org.datamate.identity.application.dto.CreateUserRequest;
import org.datamate.identity.application.dto.UserDto;
import org.datamate.identity.application.port.in.UserManagementUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserManagementUseCase userManagementUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @AuditLog(action = "CREATE_USER", resource = "USER", resourceId = "#request.userName", description = "Create user account")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.userName(),
                request.email(),
                request.phoneNumber(),
                request.firstName(),
                request.lastName(),
                request.password(),
                request.referenceSystem(),
                request.referenceValue()
        );
        UserDto createdUser = userManagementUseCase.createUser(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(userManagementUseCase.listUsers());
    }
}
