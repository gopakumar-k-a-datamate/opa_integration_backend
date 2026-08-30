package org.datamate.identity.role.adapter.in.rest.controller;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.role.application.dto.role.RoleDto;
import org.datamate.identity.role.application.dto.role.RoleRequest;
import org.datamate.identity.role.application.dto.role.RoleSelectDto;
import org.datamate.identity.role.application.port.in.role.CreateRoleUseCase;
import org.datamate.identity.role.application.port.in.role.GetRoleUseCase;
import org.datamate.identity.role.application.port.in.role.ListRolesUseCase;
import org.datamate.identity.role.application.port.in.role.SelectRolesUseCase;
import org.datamate.identity.role.application.port.in.role.UpdateRoleUseCase;
import org.datamate.identity.role.application.port.in.role.ActivateRoleUseCase;
import org.datamate.identity.role.application.port.in.role.DeactivateRoleUseCase;
import org.datamate.identity.role.application.dto.role.UpdateRoleRequest;
import org.datamate.identity.role.application.service.role.AuditActorResolver;
import com.datamate.bedrock.framework.common.ddd.datatype.EntityReference;
import java.security.Principal;
import org.datamate.identity.role.application.query.role.RoleSearchCriteria;
import org.datamate.identity.shared.model.RoleStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.datamate.bedrock.framework.common.auditing.annotation.AuditLog;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    @EnableLogger
    private Logger log;

    private final CreateRoleUseCase createRoleUseCase;
    private final ListRolesUseCase listRolesUseCase;
    private final SelectRolesUseCase selectRolesUseCase;
    private final GetRoleUseCase getRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final ActivateRoleUseCase activateRoleUseCase;
    private final DeactivateRoleUseCase deactivateRoleUseCase;
    private final AuditActorResolver auditActorResolver;

    @PostMapping
    @AuditLog(action = "CREATE_ROLE", resource = "ROLE", description = "Create new role")
    @Operation(summary = "Create a new role", description = "Creates a new role with the provided name, description, and status.")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleRequest request) {
        log.info("Create role request received for '{}'", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(createRoleUseCase.createRole(request));
    }

    @GetMapping("/select")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Select roles", description = "Retrieves a simplified list (id and name) of only active roles, optionally filtered by a search query.")
    public List<RoleSelectDto> selectRoles(@RequestParam(required = false) String search) {
        log.info("Get active roles select request received with search: '{}'", search);
        return selectRolesUseCase.selectRoles(search);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get role details", description = "Retrieve a role's detailed information by their unique ID.")
    public RoleDto getRole(@PathVariable UUID id) {
        log.info("Get role request received for id {}", id);
        return getRoleUseCase.getRoleById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "List roles", description = "Retrieves a list of roles, optionally filtered by role name search query and status, with support for pagination.")
    public Paged<RoleDto> listRoles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RoleStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("List roles request received with search: '{}', status: {}, page: {}, size: {}", search, status, page, size);
        RoleSearchCriteria criteria = new RoleSearchCriteria(search, status);
        PageQuery pageQuery = new PageQuery(page, size);
        return listRolesUseCase.listRoles(criteria, pageQuery);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @AuditLog(action = "UPDATE_ROLE", resource = "ROLE", description = "Update role details")
    @Operation(summary = "Update role details", description = "Edits the name and description of an existing role.")
    public RoleDto updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : "SYSTEM";
        EntityReference<UUID> adminUserRef = auditActorResolver.resolve(username);
        log.info("Update role request received for ID: {} by admin: {}", id, username);
        return updateRoleUseCase.updateRole(id, request, adminUserRef);
    }

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    @AuditLog(action = "ACTIVATE_ROLE", resource = "ROLE", description = "Activate role")
    @Operation(summary = "Activate role", description = "Activates a role by its ID.")
    public void activateRole(@PathVariable UUID id, Principal principal) {
        String username = principal != null ? principal.getName() : "SYSTEM";
        EntityReference<UUID> adminUserRef = auditActorResolver.resolve(username);
        log.info("Activate role request received for ID: {} by admin: {}", id, username);
        activateRoleUseCase.activateRole(id, adminUserRef);
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @AuditLog(action = "DEACTIVATE_ROLE", resource = "ROLE", description = "Deactivate role")
    @Operation(summary = "Deactivate role", description = "Deactivates a role by its ID.")
    public void deactivateRole(@PathVariable UUID id, Principal principal) {
        String username = principal != null ? principal.getName() : "SYSTEM";
        EntityReference<UUID> adminUserRef = auditActorResolver.resolve(username);
        log.info("Deactivate role request received for ID: {} by admin: {}", id, username);
        deactivateRoleUseCase.deactivateRole(id, adminUserRef);
    }
}
