package org.datamate.identity.adapter.in.rest.controller;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.dto.role.RoleRequest;
import org.datamate.identity.application.port.in.role.CreateRoleUseCase;
import org.datamate.identity.application.port.in.role.ListRolesUseCase;
import org.datamate.identity.application.port.in.role.RoleManagementUseCase;
import org.datamate.identity.application.query.role.RoleSearchCriteria;
import org.datamate.identity.shared.model.RoleStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.datamate.bedrock.framework.common.auditing.annotation.AuditLog;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    @EnableLogger
    private Logger log;

    private final RoleManagementUseCase roleManagementUseCase;
    private final CreateRoleUseCase createRoleUseCase;
    private final ListRolesUseCase listRolesUseCase;

    @PostMapping
    @AuditLog(action = "CREATE_ROLE", resource = "ROLE", description = "Create new role")
    @Operation(summary = "Create a new role", description = "Creates a new role with the provided name, description, and status.")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleRequest request) {
        log.info("Create role request received for '{}'", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(createRoleUseCase.createRole(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRole(@PathVariable Long id) {
        log.info("Get role request received for id {}", id);
        return ResponseEntity.ok(roleManagementUseCase.getRole(id));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "List roles", description = "Retrieves a list of roles, optionally filtered by role name search query and status.")
    public List<RoleDto> listRoles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RoleStatus status) {
        log.info("List roles request received with search: '{}' and status: {}", search, status);
        RoleSearchCriteria criteria = new RoleSearchCriteria(search, status);
        return listRolesUseCase.listRoles(criteria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("Delete role request received for id {}", id);
        roleManagementUseCase.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
