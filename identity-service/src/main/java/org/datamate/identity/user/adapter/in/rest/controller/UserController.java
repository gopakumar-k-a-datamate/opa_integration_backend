package org.datamate.identity.user.adapter.in.rest.controller;

import com.datamate.bedrock.framework.common.auditing.annotation.AuditLog;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.user.application.dto.CreateUserRequest;
import org.datamate.identity.user.application.dto.ResetPasswordRequest;
import org.datamate.identity.user.application.dto.ChangePasswordRequest;
import org.datamate.identity.user.application.dto.UserDto;
import org.datamate.identity.user.application.port.in.CreateUserUseCase;
import org.datamate.identity.auth.application.port.in.GetLoginHistoryUseCase;
import org.datamate.identity.auth.application.dto.LoginHistoryDto;
import org.datamate.identity.user.application.port.in.ResetPasswordUseCase;
import org.datamate.identity.user.application.port.in.ChangePasswordUseCase;
import org.datamate.identity.user.application.dto.UserResponseDto;
import org.datamate.identity.user.application.query.UserSearchCriteria;
import org.datamate.identity.user.application.port.in.ListUserUseCase;
import org.datamate.identity.user.application.port.in.GetUserUseCase;
import org.datamate.identity.user.shared.model.UserStatus;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.datamate.identity.user.application.port.in.UpdateUserUseCase;
import org.datamate.identity.user.application.dto.UpdateUserRequest;
import org.datamate.identity.role.application.port.in.user.UpdateUserRolesUseCase;
import org.datamate.identity.role.application.dto.user.UpdateUserRolesRequest;

import org.datamate.identity.user.application.port.in.ActivateUserUseCase;
import org.datamate.identity.user.application.port.in.DeactivateUserUseCase;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    @EnableLogger
    private Logger log;
    private final ListUserUseCase listUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final GetLoginHistoryUseCase getLoginHistoryUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final UpdateUserRolesUseCase updateUserRolesUseCase;

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

    @GetMapping
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

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    @AuditLog(action = "ACTIVATE_USER", resource = "USER", description = "Activate user account")
    @Operation(summary = "Activate user account", description = "Activates an inactive user account.")
    public void activateUser(@PathVariable UUID id, Principal principal) {
        String adminUsername = principal != null ? principal.getName() : "SYSTEM";
        log.info("Activate user request received for ID: {} by admin: {}", id, adminUsername);
        activateUserUseCase.activateUser(id, adminUsername);
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @AuditLog(action = "DEACTIVATE_USER", resource = "USER", description = "Deactivate user account")
    @Operation(summary = "Deactivate user account", description = "Deactivates an active user account.")
    public void deactivateUser(@PathVariable UUID id, Principal principal) {
        String adminUsername = principal != null ? principal.getName() : "SYSTEM";
        log.info("Deactivate user request received for ID: {} by admin: {}", id, adminUsername);
        deactivateUserUseCase.deactivateUser(id, adminUsername);
    }
    @PostMapping("/{id}/reset-password")
    @ResponseStatus(HttpStatus.OK)
    @AuditLog(action = "RESET_USER_PASSWORD", resource = "USER", description = "Administrator reset user password")
    @Operation(summary = "Reset user password", description = "Allows a System Administrator to reset a user's password to a temporary password.")
    public UserDto resetPassword(@PathVariable UUID id, @Valid @RequestBody ResetPasswordRequest request) {
        log.info("Admin reset password request received for user ID: {}", id);
        return resetPasswordUseCase.resetPassword(id, request);
    }

    @PostMapping("/{id}/change-password")
    @ResponseStatus(HttpStatus.OK)
    @AuditLog(action = "CHANGE_USER_PASSWORD", resource = "USER", description = "User changed password")
    @Operation(summary = "Change user password", description = "Allows an authenticated user to change their password by supplying the old password.")
    public UserDto changePassword(@PathVariable UUID id, @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request received for user ID: {}", id);
        return changePasswordUseCase.changePassword(id, request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @AuditLog(action = "UPDATE_USER", resource = "USER", description = "Update user details")
    @Operation(summary = "Update user account information", description = "Edits the information of an existing user account.")
    public UserDto updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            Principal principal
    ) {
        String adminUsername = principal != null ? principal.getName() : "SYSTEM";
        log.info("Update user request received for ID: {} by admin: {}", id, adminUsername);
        return updateUserUseCase.updateUser(id, request, adminUsername);
    }

    @PutMapping("/{id}/roles")
    @ResponseStatus(HttpStatus.OK)
    @AuditLog(action = "ASSIGN_USER_ROLES", resource = "USER", description = "Update user role assignments")
    @Operation(summary = "Update user roles", description = "Assigns or removes roles for an existing user.")
    public UserDto updateUserRoles(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRolesRequest request,
            Principal principal
    ) {
        String adminUsername = principal != null ? principal.getName() : "SYSTEM";
        log.info("Update user roles request received for ID: {} by admin: {}", id, adminUsername);
        return updateUserRolesUseCase.updateUserRoles(id, request, adminUsername);
    }
    
    @GetMapping("/login-history")
    @ResponseStatus(HttpStatus.OK)
    public Paged<LoginHistoryDto> getLoginHistory(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Request received to view login history. filter: {}, page: {}, size: {}", username, page, size);
        return getLoginHistoryUseCase.getLoginHistory(username, new PageQuery(page - 1, size));
    }
}


